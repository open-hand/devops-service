package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CiCdJobService;
import io.choerodon.devops.infra.dto.CiCdJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.mapper.CiCdJobMapper;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;

public class CiCdJobServiceImpl implements CiCdJobService {
    private static final String CREATE_JOB_FAILED = "create.job.failed";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private static final String ERROR_STAGE_ID_IS_NULL = "error.stage.id.is.null";



    @Autowired
    private CiCdJobMapper ciCdJobMapper;
    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;

    @Override
    @Transactional
    public CiCdJobDTO create(CiCdJobDTO ciCdJobDTO) {
        if (ciCdJobMapper.insertSelective(ciCdJobDTO) != 1) {
            throw new CommonException(CREATE_JOB_FAILED);
        }
        return ciCdJobMapper.selectByPrimaryKey(ciCdJobDTO.getId());
    }

    @Override
    public List<CiCdJobDTO> listByPipelineId(Long ciCdPipelineId) {
        if (ciCdPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        CiCdJobDTO ciCdJobDTO = new CiCdJobDTO();
        ciCdJobDTO.setPipelineIid(ciCdPipelineId);
        return ciCdJobMapper.select(ciCdJobDTO);
    }

    @Override
    public List<CiCdJobDTO> listByStageId(Long stageId) {
        CiCdJobDTO devopsCiJobDTO = new CiCdJobDTO();
        devopsCiJobDTO.setStageId(Objects.requireNonNull(stageId));
        return ciCdJobMapper.select(devopsCiJobDTO);
    }
    @Override
    public void deleteByStageId(Long stageId) {
        if (stageId == null) {
            throw new CommonException(ERROR_STAGE_ID_IS_NULL);
        }

        List<Long> jobIds = listByStageId(stageId).stream().map(CiCdJobDTO::getId).collect(Collectors.toList());
        if (!jobIds.isEmpty()) {
            deleteMavenSettingsRecordByJobIds(jobIds);

            CiCdJobDTO ciCdJobDTO = new CiCdJobDTO();
            ciCdJobDTO.setStageId(stageId);
            ciCdJobMapper.delete(ciCdJobDTO);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMavenSettingsRecordByJobIds(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        devopsCiMavenSettingsMapper.deleteByJobIds(jobIds);
    }
}
