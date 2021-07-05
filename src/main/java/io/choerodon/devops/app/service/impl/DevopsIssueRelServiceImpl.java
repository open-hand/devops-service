package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hzero.mybatis.BatchInsertHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.app.service.DevopsIssueRelService;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.devops.infra.enums.DevopsIssueRelObjectTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsIssueRelMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
    public void addRelation(String object, Long objectId, List<Long> issueIds) {
        List<DevopsIssueRelDTO> devopsIssueRelDTOList = issueIds
                .stream()
                .map(i -> {
                    DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
                    devopsIssueRelDTO.setIssueId(i);
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
    public void addRelation(String object, Long objectId, Long issueIds) {
        DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
        devopsIssueRelDTO.setIssueId(issueIds);
        devopsIssueRelDTO.setObject(object);
        devopsIssueRelDTO.setObjectId(objectId);
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
    public void fixBranchId() {
        int totalCount = devopsIssueRelMapper.count();
        int pageNumber = 0;
        int pageSize = 100;
        int totalPage = (totalCount + pageSize - 1) / pageSize;
        do {
            LOGGER.info("=========================process:{}/{}=========================\n", pageNumber, totalPage - 1);
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            Page<DevopsIssueRelDTO> result = PageHelper.doPage(pageRequest, () -> devopsIssueRelMapper.selectAll());
            if (!CollectionUtils.isEmpty(result.getContent())) {
                List<Long> commitIds = result.getContent().stream().filter(b -> DevopsIssueRelObjectTypeEnum.COMMIT.getValue().equals(b.getObject())).map(DevopsIssueRelDTO::getObjectId).collect(Collectors.toList());

                // 查出所有commit对应的branchId
                List<DevopsBranchDTO> devopsBranchDTOList = devopsBranchService.listByCommitIs(commitIds);

                Map<Long, Long> commitIdAndBranchIdMap = devopsBranchDTOList.stream().collect(Collectors.toMap(DevopsBranchDTO::getCommitId, DevopsBranchDTO::getId));

                // 设置关联关系中的branchId
                List<DevopsIssueRelDTO> devopsIssueRelDTOList = result.getContent().stream().peek(b -> {
                    if (DevopsIssueRelObjectTypeEnum.COMMIT.getValue().equals(b.getObject())) {
                        b.setBranchId(commitIdAndBranchIdMap.get(b.getObjectId()));
                    } else {
                        b.setBranchId(b.getObjectId());
                    }
                }).collect(Collectors.toList());

                // 更新branchId
                List<DevopsIssueRelDTO> devopsIssueRelDTOListToUpdate = devopsIssueRelDTOList.stream().filter(i -> i.getBranchId() != null).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(devopsIssueRelDTOListToUpdate)) {
                    devopsIssueRelMapper.batchUpdate(devopsIssueRelDTOListToUpdate);
                }
            }
            pageNumber++;
        } while (pageNumber < totalPage);
    }
}
