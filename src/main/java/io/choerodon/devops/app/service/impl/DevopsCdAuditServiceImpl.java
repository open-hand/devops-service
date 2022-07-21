package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdAuditService;
import io.choerodon.devops.infra.dto.DevopsCdAuditDTO;
import io.choerodon.devops.infra.mapper.DevopsCdAuditMapper;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;
import io.choerodon.devops.infra.mapper.DevopsCdStageMapper;

@Service
public class DevopsCdAuditServiceImpl implements DevopsCdAuditService {

    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;
    @Autowired
    private DevopsCdStageMapper devopsCdStageMapper;
    @Autowired
    private DevopsCdJobMapper devopsCdJobMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsCdAuditDTO devopsCdAuditDTO) {
        if (devopsCdAuditMapper.insertSelective(devopsCdAuditDTO) != 1) {
            throw new CommonException("error.insert.pipeline.user");
        }
    }


    @Override
    public List<DevopsCdAuditDTO> baseListByOptions(Long pipelineId, Long stageId, Long taskId) {
        DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO(pipelineId, stageId, taskId);
        return devopsCdAuditMapper.select(devopsCdAuditDTO);
    }
}