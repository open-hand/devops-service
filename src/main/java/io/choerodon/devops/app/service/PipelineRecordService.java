package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.cd.PipelineRecordVO;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 流水线执行记录(PipelineRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:02
 */
public interface PipelineRecordService {


    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineRecordDTO pipelineRecordDTO);

    PipelineRecordDTO baseQueryById(Long pipelineRecordId);

    void baseUpdate(PipelineRecordDTO pipelineRecordDTO);

    /**
     * 更新流水线状态到终结状态（成功或失败）
     *
     * @param pipelineRecordId
     * @param status
     */
    void updateToEndStatus(Long pipelineRecordId, PipelineStatusEnum status);

    void updateStatus(Long pipelineRecordId, String status);

    void startNextStage(PipelineRecordDTO pipelineRecordDTO, PipelineStageRecordDTO firstStageRecordDTO, List<PipelineJobRecordDTO> firstJobRecordList);

    void startNextStage(Long nextStageRecordId);

    Page<PipelineRecordVO> paging(Long projectId, Long pipelineId, PageRequest pageable);

}

