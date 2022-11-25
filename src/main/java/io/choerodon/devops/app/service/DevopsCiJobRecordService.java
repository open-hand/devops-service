package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
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
    DevopsCiJobRecordDTO queryByAppServiceIdAndGitlabJobId(Long appServiceId, Long gitlabJobId);

    void update(JobWebHookVO jobWebHookVO, String token);

    void baseUpdate(DevopsCiJobRecordDTO devopsCiJobRecordDTO);

    void deleteByPipelineId(Long ciPipelineId);

    /**
     * 根据gitlab_project_id删除job record
     */
    void deleteByAppServiceId(Long appServiceId);

    /**
     * 保存多条job记录
     */
    void create(Long ciPipelineRecordId, Long gitlabProjectId, List<JobDTO> jobDTOS, Long iamUserId, Long appServiceId);

    /**
     * 保存一条job记录
     */
    void create(Long ciPipelineRecordId,
                Long gitlabProjectId,
                JobDTO jobDTO, Long iamUserId, Long appServiceId);

    void create(Long ciPipelineId,
                Long ciPipelineRecordId,
                Long gitlabProjectId,
                JobDTO jobDTO,
                Long iamUserId,
                Long appServiceId,
                Map<String, DevopsCiJobDTO> jobMap);

    void baseCreate(DevopsCiJobRecordDTO devopsCiJobRecordDTO);

    /**
     * 根据流水线纪录id获取job纪录的数量
     *
     * @param ciPipelineRecordId 流水线纪录id
     * @return job纪录数量
     */
    int selectCountByCiPipelineRecordId(Long ciPipelineRecordId);

    List<DevopsCiJobRecordDTO> listByCiPipelineRecordId(Long ciPipelineRecordId);

    DevopsCiJobRecordDTO baseQueryById(Long id);

    AuditResultVO auditJob(Long projectId, Long id, String result);

    AduitStatusChangeVO checkAuditStatus(Long projectId, Long id);

    void updateApiTestTaskRecordInfo(String token, Long gitlabJobId, Long configId, Long apiTestTaskRecordId);

    DevopsCiJobRecordDTO syncJobRecord(Long gitlabJobId, Long appServiceId, Long ciPipelineRecordId, Long ciPipelineId, Integer gitlabProjectId);

    Long checkAndGetTriggerUserId(String token, Long gitlabJobId);

    void testResultNotify(String token, Long gitlabJobId, String successRate);
}
