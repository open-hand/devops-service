package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiCdJobDTO;

public interface CiCdJobService {
    CiCdJobDTO create(CiCdJobDTO ciCdJobDTO);

    List<CiCdJobDTO> listByPipelineId(Long ciCdPipelineId);
    /**
     * 根据stage查询job列表
     *
     * @param stageId stage的id
     * @return job列表
     */
    List<CiCdJobDTO> listByStageId(Long stageId);
    /**
     * 根据job id列表批量删除纪录
     *
     * @param jobIds 猪齿鱼job id 列表
     */
    void deleteMavenSettingsRecordByJobIds(List<Long> jobIds);

    void deleteByStageId(Long stageId);

    void deleteByPipelineId(Long ciCdPipelineId);
}
