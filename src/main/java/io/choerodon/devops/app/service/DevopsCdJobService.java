package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.CdApiTestConfigForSagaVO;
import io.choerodon.devops.api.vo.DevopsCdJobVO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.enums.JobTypeEnum;

public interface DevopsCdJobService {
    DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO);

    /**
     * 根据流水线id,查询job列表
     * @param pipelineId
     * @return
     */
    List<DevopsCdJobDTO> listByPipelineId(Long pipelineId);

    /**
     * 根据流水线id,查询job列表
     * @param jobTypeEnum
     * @return
     */
    List<DevopsCdJobDTO> listByType(JobTypeEnum jobTypeEnum);


    void deleteByStageId(Long stageId);

    void deleteByPipelineId(Long pipelineId);

    DevopsCdJobDTO queryById(Long stageId);

    void baseUpdate(DevopsCdJobDTO devopsCdJobDTO);

    void baseDelete(Long id);

    List<DevopsCdJobDTO> listByProjectIdAndType(Long projectId, JobTypeEnum typeEnum);

    List<DevopsCdJobVO> listByIdsWithNames(Set<Long> jobIds);

    /**
     * 修复数据使用，查询所有apiTest类型的数据
     *
     * @return
     */
    List<CdApiTestConfigForSagaVO> listCdApiTestConfig();


    List<DevopsCdJobDTO> listByStageId(Long stageId);
}
