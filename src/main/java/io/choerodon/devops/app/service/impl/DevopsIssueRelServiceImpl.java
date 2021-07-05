package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.IssueIdAndBranchIdsVO;
import io.choerodon.devops.app.service.DevopsIssueRelService;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.devops.infra.mapper.DevopsIssueRelMapper;

@Service
public class DevopsIssueRelServiceImpl implements DevopsIssueRelService {
    @Autowired
    private DevopsIssueRelMapper devopsIssueRelMapper;

    @Autowired
    @Qualifier("devopsIssueRelBatchInsertHelper")
    private BatchInsertHelper<DevopsIssueRelDTO> batchInsertHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRelation(String object, Long objectId, Long branchId, List<Long> issueIds) {
        List<DevopsIssueRelDTO> devopsIssueRelDTOList = issueIds
                .stream()
                .map(i -> {
                    DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
                    devopsIssueRelDTO.setIssueId(i);
                    devopsIssueRelDTO.setObject(object);
                    devopsIssueRelDTO.setObjectId(objectId);
                    devopsIssueRelDTO.setBranchId(branchId);
                    return devopsIssueRelDTO;
                }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(devopsIssueRelDTOList)) {
            batchInsertHelper.batchInsert(devopsIssueRelDTOList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addRelation(String object, Long objectId, Long branchId, Long issueId) {
        DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
        devopsIssueRelDTO.setIssueId(issueId);
        devopsIssueRelDTO.setObject(object);
        devopsIssueRelDTO.setObjectId(objectId);
        devopsIssueRelDTO.setBranchId(branchId);
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
    public Set<Long> listObjectIdsByIssueIdAndObjectType(String object, Long issueId) {
        return devopsIssueRelMapper.listObjectIdsByIssueIdAndObjectType(object, issueId);
    }

    @Override
    public List<IssueIdAndBranchIdsVO> listObjectIdsByIssueIdsAndObjectType(String object, Set<Long> issueIds) {
        if (CollectionUtils.isEmpty(issueIds)) {
            return new ArrayList<>();
        }
        List<IssueIdAndBranchIdsVO> result = new ArrayList<>();
        List<DevopsIssueRelDTO> devopsIssueRelDTOList = devopsIssueRelMapper.listObjectIdsByIssueIdsAndObjectType(object, issueIds);
        devopsIssueRelDTOList
                .stream()
                .collect(Collectors.groupingBy(DevopsIssueRelDTO::getIssueId, Collectors.mapping(DevopsIssueRelDTO::getObjectId, Collectors.toList())))
                .forEach((k, v) -> {
                    IssueIdAndBranchIdsVO issueIdAndBranchIdsVO = new IssueIdAndBranchIdsVO();
                    issueIdAndBranchIdsVO.setIssueId(k);
                    issueIdAndBranchIdsVO.setBranchIds(v);
                    result.add(issueIdAndBranchIdsVO);
                });
        return result;
    }
}
