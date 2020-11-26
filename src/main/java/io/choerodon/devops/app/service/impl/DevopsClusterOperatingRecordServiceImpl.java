package io.choerodon.devops.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsClusterOperatingRecordService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.dto.DevopsClusterOperationRecordDTO;
import io.choerodon.devops.infra.enums.ClusterOperationStatusEnum;
import io.choerodon.devops.infra.mapper.DevopsClusterOperationRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/27 17:50
 */
@Service
public class DevopsClusterOperatingRecordServiceImpl implements DevopsClusterOperatingRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsClusterOperatingRecordServiceImpl.class);
    @Autowired
    private DevopsClusterOperationRecordMapper devopsClusterOperationRecordMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DevopsClusterOperationRecordDTO saveOperatingRecord(Long clusterId, Long nodeId, String operatingType, String status, String errorMsg) {
        DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = new DevopsClusterOperationRecordDTO();
        devopsClusterOperationRecordDTO.setClusterId(clusterId);
        devopsClusterOperationRecordDTO.setNodeId(nodeId);
        devopsClusterOperationRecordDTO.setType(operatingType);
        devopsClusterOperationRecordDTO.setStatus(status);
        devopsClusterOperationRecordDTO.setErrorMsg(errorMsg);
        devopsClusterOperationRecordMapper.insert(devopsClusterOperationRecordDTO);
        return devopsClusterOperationRecordMapper.selectByPrimaryKey(devopsClusterOperationRecordDTO.getId());
    }

    @Override
    public DevopsClusterOperationRecordDTO queryLatestRecordByNodeId(Long nodeId) {
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);

        return devopsClusterOperationRecordMapper.queryLatestRecordByNodeId(nodeId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusInNewTrans(Long operationRecordId, ClusterOperationStatusEnum statusEnum, String errorMsg) {
        DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperationRecordMapper.selectByPrimaryKey(operationRecordId);
        devopsClusterOperationRecordDTO.setStatus(statusEnum.value());
        devopsClusterOperationRecordDTO.setErrorMsg(errorMsg);
        devopsClusterOperationRecordMapper.updateByPrimaryKeySelective(devopsClusterOperationRecordDTO);
    }

    @Override
    public DevopsClusterOperationRecordDTO queryById(Long operationRecordId) {
        return devopsClusterOperationRecordMapper.selectByPrimaryKey(operationRecordId);
    }
}
