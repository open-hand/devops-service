package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
@Service
public class DevopsCiJobServiceImpl implements DevopsCiJobService {
    private static final String CREATE_JOB_FAILED = "create.job.failed";
    private static final String DELETE_JOB_FAILED = "delete.job.failed";
    private static final String ERROR_STAGE_ID_IS_NULL = "error.stage.id.is.null";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private DevopsCiJobMapper devopsCiJobMapper;

    public DevopsCiJobServiceImpl(DevopsCiJobMapper devopsCiJobMapper) {
        this.devopsCiJobMapper = devopsCiJobMapper;
    }

    @Override
    @Transactional
    public DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO) {
        if (devopsCiJobMapper.insertSelective(devopsCiJobDTO) != 1) {
            throw new CommonException(CREATE_JOB_FAILED);
        }
        return devopsCiJobMapper.selectByPrimaryKey(devopsCiJobDTO.getId());
    }

    @Override
    @Transactional
    public void deleteByStageId(Long stageId) {
        if (stageId == null) {
            throw new CommonException(ERROR_STAGE_ID_IS_NULL);
        }
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiStageId(stageId);
        devopsCiJobMapper.delete(devopsCiJobDTO);
    }

    @Override
    public List<DevopsCiJobDTO> listByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        return devopsCiJobMapper.select(devopsCiJobDTO);
    }
}
