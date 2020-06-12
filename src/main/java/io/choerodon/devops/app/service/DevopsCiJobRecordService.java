package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:27
 */
public interface DevopsCiJobRecordService {
    /**
     * 根据gitlab-Job id查询job执行记录
     */
    DevopsCiJobRecordDTO queryByGitlabJobId(Long gitlabJobId);

    void update(JobWebHookVO jobWebHookVO);

    void deleteByPipelineId(Long ciPipelineId);

    /**
     * 根据gitlab_project_id删除job record
     */
    void deleteByGitlabProjectId(Long gitlabProjectId);

    /**
     * 保存多条job记录
     */
    void create(Long ciPipelineRecordId, Long gitlabProjectId, List<JobDTO> jobDTOS, Long iamUserId);

    /**
     * 保存一条job记录
     */
    void create(Long ciPipelineRecordId, Long gitlabProjectId, JobDTO jobDTO, Long iamUserId);

    /**
     * 根据流水线纪录id获取job纪录的数量
     *
     * @param ciPipelineRecordId 流水线纪录id
     * @return job纪录数量
     */
    int selectCountByCiPipelineRecordId(Long ciPipelineRecordId);
}
