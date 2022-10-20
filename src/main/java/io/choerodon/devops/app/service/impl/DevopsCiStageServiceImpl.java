package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiStageVO;
import io.choerodon.devops.app.service.DevopsCiStageService;
import io.choerodon.devops.infra.dto.DevopsCiStageDTO;
import io.choerodon.devops.infra.mapper.DevopsCiStageMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:23
 */
@Service
public class DevopsCiStageServiceImpl implements DevopsCiStageService {

    private static final String DEVOPS_CREATE_STAGE_FAILED = "devops.create.stage.failed";
    private static final String DEVOPS_DELETE_STAGE_FAILED = "devops.delete.stage.failed";
    private DevopsCiStageMapper devopsCiStageMapper;

    public DevopsCiStageServiceImpl(DevopsCiStageMapper devopsCiStageMapper) {
        this.devopsCiStageMapper = devopsCiStageMapper;
    }

    @Override
    @Transactional
    public DevopsCiStageDTO create(DevopsCiStageDTO devopsCiStageDTO) {
        devopsCiStageDTO.setId(null);
        if (devopsCiStageMapper.insertSelective(devopsCiStageDTO) != 1) {
            throw new CommonException(DEVOPS_CREATE_STAGE_FAILED);
        }
        return devopsCiStageMapper.selectByPrimaryKey(devopsCiStageDTO.getId());
    }

    @Override
    public List<DevopsCiStageDTO> listByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }
        DevopsCiStageDTO devopsCiStageDTO = new DevopsCiStageDTO();
        devopsCiStageDTO.setCiPipelineId(ciPipelineId);
        return devopsCiStageMapper.select(devopsCiStageDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (devopsCiStageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(DEVOPS_DELETE_STAGE_FAILED);
        }
    }

    @Override
    @Transactional
    public void update(DevopsCiStageVO devopsCiStageVO) {
        DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
        // 不允许修改所属流水线
        devopsCiStageDTO.setCiPipelineId(null);
        devopsCiStageMapper.updateByPrimaryKeySelective(devopsCiStageDTO);
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }
        DevopsCiStageDTO record = new DevopsCiStageDTO();
        record.setCiPipelineId(ciPipelineId);
        devopsCiStageMapper.delete(record);
    }

}
