package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.PipelineHomeVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineTriggerTypeEnum;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 流水线表(Pipeline)应用服务
 *
 * @author
 * @since 2022-11-24 15:50:13
 */
public interface PipelineService {
    void baseCreate(PipelineDTO pipelineDTO);

    void baseUpdate(PipelineDTO pipelineDTO);

    void updateEffectVersionId(Long id, Long effectVersionId);

    void baseDeleteById(Long id);

    PipelineDTO baseQueryById(Long id);

    PipelineDTO baseQueryByToken(String token);

    PipelineDTO queryByTokenOrThrowE(String token);

    PipelineDTO create(Long projectId, PipelineVO pipelineVO);

    void enable(Long projectId, Long id);

    void disable(Long projectId, Long id);

    void delete(Long projectId, Long id);

    void update(Long projectId, Long id, PipelineVO pipelineVO);

    PipelineRecordDTO executeByManual(Long projectId, Long id);

    PipelineRecordDTO execute(Long id,
                              PipelineTriggerTypeEnum triggerType,
                              Map<String, Object> params);


    PipelineRecordDTO executeByToken(Long projectId, String token, String personalToken);

    PipelineVO query(Long projectId, Long id);

    Page<PipelineHomeVO> paging(Long projectId, PageRequest pageRequest, Boolean enable, String status, String triggerType, String param);

    Boolean checkName(Long projectId, Long id, String name);

    void triggerByAppVersion(Long appServiceId, Long appVersionId);

    List<PipelineInstanceReferenceVO> listDeployValuePipelineReference(Long projectId, Long valueId);

    List<PipelineInstanceReferenceVO> listAppPipelineReference(Long projectId, Long appId);

    int countAppServicePipelineReference(Long projectId, Long appServiceId);


}

