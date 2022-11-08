package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:26
 */
public interface DevopsCiPipelineRecordService {
    void create(PipelineWebHookVO pipelineWebHookVO, String token);

    void handleCreate(PipelineWebHookVO pipelineWebHookVO);

    /**
     * 异步地拉取gitlab中流水线的状态到数据库进行更新
     *
     * @param pipelineRecordId 猪齿鱼流水线纪录id
     * @param gitlabPipelineId gitlab流水线id
     */
    void asyncPipelineUpdate(Long pipelineRecordId, Integer gitlabPipelineId);

    DevopsCiPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long ciPipelineRecordId);

    /**
     * 删除流水线的执行记录
     */
    void deleteByPipelineId(Long ciPipelineId);

    /**
     * 查询流水线执行记录
     */
    List<DevopsCiPipelineRecordDTO> queryByPipelineId(Long ciPipelineId);

    DevopsCiPipelineRecordDTO create(Long ciPipelineId, Long gitlabProjectId, Pipeline pipeline);

    /**
     * 重试流水线
     */
    void retry(Long projectId, Long gitlabPipelineId, Long gitlabProjectId);

    /**
     * 取消执行流水线
     */
    void cancel(Long projectId, Long gitlabPipelineId, Long gitlabProjectId);

    /**
     * 查询流水线执行记录
     */
    DevopsCiPipelineRecordDTO queryById(Long ciPipelineRecordId);

    DevopsCiPipelineRecordDTO queryByGitlabPipelineId(Long devopsPipelineId, Long gitlabPipelineId);

    List<DevopsCiPipelineRecordDTO> queryNotSynchronizedRecord(Long statusUpdatePeriodMilliSeconds);

    DevopsCiPipelineRecordVO queryByCiPipelineRecordId(Long ciPipelineRecordId);

    DevopsCiPipelineRecordDTO queryByAppServiceIdAndGitlabPipelineId(Long appServiceId, Long gitlabPipelineId);

    List<CiPipelineRecordVO> listByPipelineId(Long pipelineId);

    /**
     * 为流水线记录添加阶段，任务等信息
     *
     * @param recordVO
     */
    void fillAdditionalInfo(CiPipelineRecordVO recordVO);
}
