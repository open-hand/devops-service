package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.hzero.mybatis.BatchInsertHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.IssueIdAndBranchIdsVO;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.app.service.DevopsIssueRelService;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.devops.infra.enums.DevopsIssueRelObjectTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsIssueRelMapper;

@Service
public class DevopsIssueRelServiceImpl implements DevopsIssueRelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsIssueRelServiceImpl.class);
    @Autowired
    private DevopsIssueRelMapper devopsIssueRelMapper;

    @Autowired
    private DevopsBranchService devopsBranchService;

    @Autowired
    @Qualifier("devopsIssueRelBatchInsertHelper")
    private BatchInsertHelper<DevopsIssueRelDTO> batchInsertHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRelation(String object, Long objectId, Long branchId, Long projectId, String appServiceCode, List<Long> issueIds) {
        List<DevopsIssueRelDTO> devopsIssueRelDTOList = issueIds
                .stream()
                .map(i -> {
                    DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
                    devopsIssueRelDTO.setIssueId(i);
                    devopsIssueRelDTO.setBranchId(branchId);
                    devopsIssueRelDTO.setProjectId(projectId);
                    devopsIssueRelDTO.setAppServiceCode(appServiceCode);
                    devopsIssueRelDTO.setObject(object);
                    devopsIssueRelDTO.setObjectId(objectId);
                    return devopsIssueRelDTO;
                }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(devopsIssueRelDTOList)) {
            batchInsertHelper.batchInsert(devopsIssueRelDTOList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addRelation(String object, Long objectId, Long branchId, Long projectId, String appServiceCode, Long issueId) {
        DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
        devopsIssueRelDTO.setIssueId(issueId);
        devopsIssueRelDTO.setObject(object);
        devopsIssueRelDTO.setObjectId(objectId);
        devopsIssueRelDTO.setProjectId(projectId);
        devopsIssueRelDTO.setBranchId(branchId);
        devopsIssueRelDTO.setAppServiceCode(appServiceCode);
        devopsIssueRelMapper.insert(devopsIssueRelDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationByObjectAndObjectId(String object, Long objectId) {
        DevopsIssueRelDTO devopsIssueRelDTOToDelete = new DevopsIssueRelDTO();
        devopsIssueRelDTOToDelete.setObject(object);
        devopsIssueRelDTOToDelete.setObjectId(objectId);
        devopsIssueRelMapper.delete(devopsIssueRelDTOToDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationByObjectAndObjectIdAndIssueId(String object, Long objectId, Long issueId) {
        DevopsIssueRelDTO devopsIssueRelDTOToDelete = new DevopsIssueRelDTO();
        devopsIssueRelDTOToDelete.setIssueId(issueId);
        devopsIssueRelDTOToDelete.setObjectId(objectId);
        devopsIssueRelDTOToDelete.setObject(object);
        devopsIssueRelMapper.delete(devopsIssueRelDTOToDelete);
    }

    @Override
    public Map<Long, List<Long>> listMappedIssueIdsByObjectTypeAndObjectId(String object, Set<Long> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return new HashMap<>();
        }
        List<DevopsIssueRelDTO> devopsIssueRelDTOS = devopsIssueRelMapper.listIssueIdsByObjectTypeAndObjectIds(objectIds, object);
        return devopsIssueRelDTOS.stream().collect(Collectors.groupingBy(DevopsIssueRelDTO::getObjectId, Collectors.mapping(DevopsIssueRelDTO::getIssueId, Collectors.toList())));
    }

    @Override
    public Set<DevopsIssueRelDTO> listRelationByIssueIdAndObjectType(Long projectId, String object, Long issueId) {
        return devopsIssueRelMapper.listRelationByIssueIdAndObjectType(projectId, object, issueId);
    }

    @Override
    public List<IssueIdAndBranchIdsVO> listBranchInfoByIssueIds(Set<Long> issueIds) {
        if (CollectionUtils.isEmpty(issueIds)) {
            return new ArrayList<>();
        }
        List<IssueIdAndBranchIdsVO> result = new ArrayList<>();
        List<DevopsIssueRelDTO> devopsIssueRelDTOList = devopsIssueRelMapper.listRelationByIssueIdsAndObjectType(DevopsIssueRelObjectTypeEnum.COMMIT.getValue(), issueIds);
        devopsIssueRelDTOList
                .stream()
                .collect(Collectors.groupingBy(DevopsIssueRelDTO::getIssueId, Collectors.mapping(r -> {
                    DevopsBranchVO devopsBranchVO = new DevopsBranchVO();
                    devopsBranchVO.setProjectId(r.getProjectId());
                    devopsBranchVO.setAppServiceCode(r.getAppServiceCode());
                    devopsBranchVO.setBranchId(r.getBranchId());
                    return devopsBranchVO;
                }, Collectors.toList())))
                .forEach((k, v) -> {
                    IssueIdAndBranchIdsVO issueIdAndBranchIdsVO = new IssueIdAndBranchIdsVO();
                    issueIdAndBranchIdsVO.setIssueId(k);
                    issueIdAndBranchIdsVO.setBranches(v);
                    result.add(issueIdAndBranchIdsVO);
                });
        return result;
    }

    @Override
    public List<Long> listExistRelationBranchIds(Set<Long> commitRelatedBranchIds) {
        if (CollectionUtils.isEmpty(commitRelatedBranchIds)) {
            return new ArrayList<>();
        }
        return devopsIssueRelMapper.listExistRelationBranchIds(commitRelatedBranchIds);
    }

    @Override
    public List<Long> listBranchIdsByCommitIds(Set<Long> commitIds) {
        if (CollectionUtils.isEmpty(commitIds)) {
            return new ArrayList<>();
        }
        return devopsIssueRelMapper.listBranchIdsByCommitIds(commitIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommitRelationByBranchId(Long branchId, Long issueId) {
        devopsIssueRelMapper.deleteCommitRelationByBranchIdAndIssueId(branchId, issueId);
    }

    @Override
    public List<Long> listCommitRelationByBranchId(Long branchId) {
        return devopsIssueRelMapper.listCommitRelationByBranchId(branchId);
    }
}
