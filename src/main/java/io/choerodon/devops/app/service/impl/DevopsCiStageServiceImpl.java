package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiStageVO;
import io.choerodon.devops.app.service.DevopsCiStageService;
import io.choerodon.devops.infra.dto.DevopsCiStageDTO;
import io.choerodon.devops.infra.mapper.DevopsCiStageMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:23
 */
@Service
public class DevopsCiStageServiceImpl implements DevopsCiStageService {

    private static final String CREATE_STAGE_FAILED = "create.stage.failed";
    private static final String DELETE_STAGE_FAILED = "delete.stage.failed";
    private static final String UPDATE_STAGE_FAILED = "update.stage.failed";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private DevopsCiStageMapper devopsCiStageMapper;

    public DevopsCiStageServiceImpl(DevopsCiStageMapper devopsCiStageMapper) {
        this.devopsCiStageMapper = devopsCiStageMapper;
    }

    @Override
    @Transactional
    public DevopsCiStageDTO create(DevopsCiStageDTO devopsCiStageDTO) {
        if (devopsCiStageMapper.insertSelective(devopsCiStageDTO) != 1) {
            throw new CommonException(CREATE_STAGE_FAILED);
        }
        return devopsCiStageMapper.selectByPrimaryKey(devopsCiStageDTO.getId());
    }

    @Override
    public List<DevopsCiStageDTO> listByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiStageDTO devopsCiStageDTO = new DevopsCiStageDTO();
        devopsCiStageDTO.setCiPipelineId(ciPipelineId);
        return devopsCiStageMapper.select(devopsCiStageDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (devopsCiStageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(DELETE_STAGE_FAILED);
        }
    }

    @Override
    @Transactional
    public void update(DevopsCiStageVO devopsCiStageVO) {
        DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
        devopsCiStageMapper.updateByPrimaryKeySelective(devopsCiStageDTO);
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiStageDTO record = new DevopsCiStageDTO();
        record.setCiPipelineId(ciPipelineId);
        devopsCiStageMapper.delete(record);
    }

}
