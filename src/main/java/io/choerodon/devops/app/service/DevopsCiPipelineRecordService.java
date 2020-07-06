package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
     * 分页查询流水线记录
     */
    Page<DevopsCiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, PageRequest pageable);

    /**
     * 异步地拉取gitlab中流水线的状态到数据库进行更新
     *
     * @param pipelineRecordId 猪齿鱼流水线纪录id
     * @param gitlabPipelineId gitlab流水线id
     */
    void asyncPipelineUpdate(Long pipelineRecordId, Integer gitlabPipelineId);

    DevopsCiPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId);

    /**
     * 删除流水线的执行记录
     */
    void deleteByPipelineId(Long ciPipelineId);

    /**
     * 查询流水线执行记录
     */
    List<DevopsCiPipelineRecordDTO> queryByPipelineId(Long ciPipelineId);

    /**
     * 根据gitlabProjectId删除pipeline record
     */
    void deleteByGitlabProjectId(Long gitlabProjectId);

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

    DevopsCiPipelineRecordDTO queryByGitlabPipelineId(Long gitlabPipelineId);
}
