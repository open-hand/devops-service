package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hzero.boot.message.entity.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdAuditRecordService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.dto.DevopsCdAuditRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCdAuditRecordMapper;

/**
 * @author scp
 * @date 2020/7/3
 * @description
 */
@Service
public class DevopsCdAuditRecordServiceImpl implements DevopsCdAuditRecordService {

    private static final String STAGE_NAME = "stageName";

    private static final String ERROR_UPDATE_AUDIT_RECORD = "error.update.audit.record";

    @Autowired
    private DevopsCdAuditRecordMapper devopsCdAuditRecordMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SendNotificationService sendNotificationService;

    @Override
    public List<DevopsCdAuditRecordDTO> queryByStageRecordId(Long stageRecordId) {
        DevopsCdAuditRecordDTO auditRecordDTO = new DevopsCdAuditRecordDTO();
        auditRecordDTO.setStageRecordId(stageRecordId);
        return devopsCdAuditRecordMapper.select(auditRecordDTO);
    }

    @Override
    public List<DevopsCdAuditRecordDTO> queryByJobRecordId(Long jobRecordId) {
        DevopsCdAuditRecordDTO auditRecordDTO = new DevopsCdAuditRecordDTO();
        auditRecordDTO.setJobRecordId(jobRecordId);
        return devopsCdAuditRecordMapper.select(auditRecordDTO);
    }

    @Override
    public void sendStageAuditMessage(DevopsCdStageRecordDTO devopsCdStageRecord) {
        // 查询审核人员
        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = queryByStageRecordId(devopsCdStageRecord.getId());
        if (CollectionUtils.isEmpty(devopsCdAuditRecordDTOS)) {
            return;
        }
        // 发送审核通知
        List<Receiver> userList = new ArrayList<>();
        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        userIds.forEach(id -> {
            IamUserDTO iamUserDTO = userDTOMap.get(id);
            if (iamUserDTO != null) {
                Receiver user = new Receiver();
                user.setEmail(iamUserDTO.getEmail());
                user.setUserId(iamUserDTO.getId());
                user.setPhone(iamUserDTO.getPhone());
                user.setTargetUserTenantId(iamUserDTO.getOrganizationId());
                userList.add(user);
            }
        });
        HashMap<String, String> params = new HashMap<>();
        params.put(STAGE_NAME, devopsCdStageRecord.getStageName());
        sendNotificationService.sendPipelineNotice(devopsCdStageRecord.getPipelineRecordId(), MessageCodeConstants.PIPELINE_AUDIT, userList, params);
    }

    @Override
    public void sendJobAuditMessage(Long pipelineRecordId, DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        // 查询审核人员
        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = queryByJobRecordId(devopsCdJobRecordDTO.getId());
        if (CollectionUtils.isEmpty(devopsCdAuditRecordDTOS)) {
            return;
        }
        // 发送审核通知
        List<Receiver> userList = new ArrayList<>();
        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        userIds.forEach(id -> {
            IamUserDTO iamUserDTO = userDTOMap.get(id);
            if (iamUserDTO != null) {
                Receiver user = new Receiver();
                user.setEmail(iamUserDTO.getEmail());
                user.setUserId(iamUserDTO.getId());
                user.setPhone(iamUserDTO.getPhone());
                user.setTargetUserTenantId(iamUserDTO.getOrganizationId());
                userList.add(user);
            }
        });
        HashMap<String, String> params = new HashMap<>();
        params.put(STAGE_NAME, devopsCdJobRecordDTO.getName());
        sendNotificationService.sendPipelineNotice(pipelineRecordId, MessageCodeConstants.PIPELINE_AUDIT, userList, params);
    }

    @Override
    @Transactional
    public void update(DevopsCdAuditRecordDTO devopsCdAuditRecordDTO) {
        if (devopsCdAuditRecordMapper.updateByPrimaryKeySelective(devopsCdAuditRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_AUDIT_RECORD);
        }
    }
}
