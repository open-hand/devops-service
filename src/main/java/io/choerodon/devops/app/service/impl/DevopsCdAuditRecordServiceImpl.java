package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCdAuditRecordService;
import io.choerodon.devops.infra.dto.DevopsCdAuditRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsCdAuditRecordMapper;

/**
 * @author scp
 * @date 2020/7/3
 * @description
 */
@Service
public class DevopsCdAuditRecordServiceImpl implements DevopsCdAuditRecordService {
    @Autowired
    private DevopsCdAuditRecordMapper devopsCdAuditRecordMapper;

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
}
