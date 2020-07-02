package io.choerodon.devops.app.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiCdStageVO;
import io.choerodon.devops.app.service.CiCdStageService;
import io.choerodon.devops.infra.dto.CiCdStageDTO;
import io.choerodon.devops.infra.dto.DevopsCiStageDTO;
import io.choerodon.devops.infra.mapper.CiCdStageMapper;
import io.choerodon.devops.infra.mapper.DevopsCdAuditMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

@Service
public class CiCdStageServiceImpl implements CiCdStageService {
    private static final String CREATE_STAGE_FAILED = "create.stage.failed";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private static final String DELETE_STAGE_FAILED = "delete.stage.failed";
    private static final String UPDATE_STAGE_FAILED = "update.stage.failed";



    @Autowired
    private CiCdStageMapper ciCdStageMapper;
    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;

    @Override
    @Transactional
    public CiCdStageDTO create(CiCdStageDTO ciCdStageDTO) {
        if (ciCdStageMapper.insert(ciCdStageDTO) != 1) {
            throw new CommonException(CREATE_STAGE_FAILED);
        }
        return ciCdStageMapper.selectByPrimaryKey(ciCdStageDTO.getId());
    }

    @Override
    public List<CiCdStageDTO> listByPipelineId(Long ciCdPipelineId) {
        if (ciCdPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        CiCdStageDTO ciCdStageDTO = new CiCdStageDTO();
        ciCdStageDTO.setPipelineId(ciCdPipelineId);
        List<CiCdStageDTO> ciCdStageDTOS = ciCdStageMapper.select(ciCdStageDTO);
        return ciCdStageDTOS;
    }

    @Override
    @Transactional
    public void deleteById(Long stageId) {
        if (ciCdStageMapper.deleteByPrimaryKey(stageId) != 1) {
            throw new CommonException(DELETE_STAGE_FAILED);
        }
    }

    @Override
    @Transactional
    public void update(CiCdStageVO ciCdStageVO) {
        CiCdStageDTO ciCdStageDTO = ConvertUtils.convertObject(ciCdStageVO, CiCdStageDTO.class);
        if (ciCdStageMapper.updateByPrimaryKeySelective(ciCdStageDTO) != 1) {
            throw new CommonException(UPDATE_STAGE_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        CiCdStageDTO record = new CiCdStageDTO();
        record.setPipelineId(ciPipelineId);
        ciCdStageMapper.delete(record);
    }


}
