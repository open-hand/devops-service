package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CiCdJobService;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
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
    public DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO) {
        if (ciCdJobMapper.insertSelective(devopsCdJobDTO) != 1) {
            throw new CommonException(CREATE_JOB_FAILED);
        }
        return ciCdJobMapper.selectByPrimaryKey(devopsCdJobDTO.getId());
    }

    @Override
    public List<DevopsCdJobDTO> listByPipelineId(Long ciCdPipelineId) {
        if (ciCdPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineId(ciCdPipelineId);
        return ciCdJobMapper.select(devopsCdJobDTO);
    }

    @Override
    public List<DevopsCdJobDTO> listByStageId(Long stageId) {
        DevopsCdJobDTO devopsCiJobDTO = new DevopsCdJobDTO();
        devopsCiJobDTO.setStageId(Objects.requireNonNull(stageId));
        return ciCdJobMapper.select(devopsCiJobDTO);
    }
    @Override
    public void deleteByStageId(Long stageId) {
        if (stageId == null) {
            throw new CommonException(ERROR_STAGE_ID_IS_NULL);
        }

        List<Long> jobIds = listByStageId(stageId).stream().map(DevopsCdJobDTO::getId).collect(Collectors.toList());
        if (!jobIds.isEmpty()) {
            deleteMavenSettingsRecordByJobIds(jobIds);

            DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
            devopsCdJobDTO.setStageId(stageId);
            ciCdJobMapper.delete(devopsCdJobDTO);
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

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciCdPipelineId) {
        if (ciCdPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        // 删除maven settings
        deleteMavenSettingsRecordByJobIds(listByPipelineId(ciCdPipelineId).stream().map(DevopsCdJobDTO::getId).collect(Collectors.toList()));
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineId(ciCdPipelineId);
        ciCdJobMapper.delete(devopsCdJobDTO);
    }

}
