package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.DevopsIssueRelService;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.devops.infra.mapper.DevopsIssueRelMapper;

public class DevopsIssueRelServiceImpl implements DevopsIssueRelService {
    @Autowired
    private DevopsIssueRelMapper devopsIssueRelMapper;

    @Autowired
    @Qualifier("devopsIssueRelBatchInsertHelper")
    private BatchInsertHelper<DevopsIssueRelDTO> batchInsertHelper;

    @Override
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
        batchInsertHelper.batchInsert(devopsIssueRelDTOList);
    }

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
    public void deleteRelation(String object, Long objectId) {
        DevopsIssueRelDTO devopsIssueRelDTOToDelete = new DevopsIssueRelDTO();
        devopsIssueRelDTOToDelete.setObject(object);
        devopsIssueRelDTOToDelete.setObjectId(objectId);
        devopsIssueRelMapper.delete(devopsIssueRelDTOToDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelation(Long issueId) {
        DevopsIssueRelDTO devopsIssueRelDTOToDelete = new DevopsIssueRelDTO();
        devopsIssueRelDTOToDelete.setIssueId(issueId);
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
}
