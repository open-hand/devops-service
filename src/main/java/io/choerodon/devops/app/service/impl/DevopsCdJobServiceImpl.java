package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdJobService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdAuditDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.mapper.DevopsCdAuditMapper;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;

@Service
public class DevopsCdJobServiceImpl implements DevopsCdJobService {
    @Autowired
    private DevopsCdJobMapper devopsCdJobMapper;
    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;


    @Override
    public DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO) {
        if (devopsCdJobMapper.insert(devopsCdJobDTO) != 1) {
            throw new CommonException("error.insert.cd.job");
        }
        return devopsCdJobMapper.selectByPrimaryKey(devopsCdJobDTO.getId());
    }

    @Override
    public List<DevopsCdJobDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineId(pipelineId);
        return devopsCdJobMapper.select(devopsCdJobDTO);
    }

    @Override
    public void deleteByStageId(Long stageId) {
        Assert.notNull(stageId, "error.cd.stage.id.is.null");
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setStageId(stageId);
        List<DevopsCdJobDTO> cdJobDTOS = devopsCdJobMapper.select(devopsCdJobDTO);
        if (!CollectionUtils.isEmpty(cdJobDTOS)) {
            cdJobDTOS.forEach(cdJobDTO -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCdJobId(cdJobDTO.getId());
                devopsCdAuditMapper.delete(devopsCdAuditDTO);
            });
            devopsCdJobMapper.delete(devopsCdJobDTO);
        }
    }

    @Override
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, "error.cd.job.pipeline.id.is.null");
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineId(pipelineId);
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobMapper.select(devopsCdJobDTO);
        if (!CollectionUtils.isEmpty(devopsCdJobDTOS)) {
            devopsCdJobDTOS.forEach(cdJobDTO -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCdJobId(cdJobDTO.getId());
                devopsCdAuditMapper.delete(devopsCdAuditDTO);
            });
            devopsCdJobMapper.delete(devopsCdJobDTO);
        }
    }

    @Override
    public String queryTrace(Long gitlabProjectId, Long jobId) {
        return null;
    }

    @Override
    public void retryJob(Long projectId, Long gitlabProjectId, Long jobId) {

    }

    @Override
    public DevopsCdJobDTO queryById(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.ERROR_STAGE_ID_IS_NULL);
        return devopsCdJobMapper.selectByPrimaryKey(stageId);
    }
}
