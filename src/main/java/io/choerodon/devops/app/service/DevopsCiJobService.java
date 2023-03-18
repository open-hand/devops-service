package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
public interface DevopsCiJobService {
    /**
     * 创建ci流水线job
     *
     * @param devopsCiJobDTO 创建信息
     * @return 创建结果
     */
    DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO);

    /**
     * 删除stage下的job
     *
     * @param stageId stageId
     */
    void deleteByStageId(Long stageId);

    /**
     * 删除stage下的job、job中的步骤、步骤关联的配置
     *
     * @param stageId stageId
     */
    void deleteByStageIdCascade(Long stageId);

    /**
     * 查询pipeline下的jobs
     *
     * @param ciPipelineId 流水线id
     * @return 结果
     */
    List<DevopsCiJobDTO> listByPipelineId(Long ciPipelineId);

    /**
     * 查询流水线中的自定义任务
     *
     * @param ciPipelineId
     * @return
     */
    List<DevopsCiJobVO> listCustomByPipelineId(Long ciPipelineId);

    /**
     * 根据stage查询job列表
     *
     * @param stageId stage的id
     * @return job列表
     */
    List<DevopsCiJobDTO> listByStageId(Long stageId);

    /**
     * sonar的连接测试
     */
    Boolean sonarConnect(Long projectId, SonarQubeConfigVO sonarQubeConfigVO);

    /**
     * 查询job日志
     */
    DevopsCiJobLogVO queryTrace(Long gitlabProjectId, Long jobId, Long appServiceId);

    /**
     * 重试job
     */
    void retryJob(Long projectId, Long gitlabProjectId, Long jobId, Long appServiceId);

    /**
     * 删除流水线下的job
     */
    void deleteByPipelineId(Long ciPipelineId);

    /**
     * 查询maven settings文件内容
     *
     * @param projectId       项目id
     * @param appServiceToken 应用服务token
     * @param jobId           job id
     * @param sequence        maven构建步骤的序列号
     * @return settings文件内容
     */
    String queryMavenSettings(Long projectId, String appServiceToken, Long jobId, Long sequence);


    /**
     * 根据job id列表批量删除纪录
     *
     * @param jobIds 猪齿鱼job id 列表
     */
    void deleteMavenSettingsRecordByJobIds(List<Long> jobIds);

    SonarInfoVO getSonarConfig(Long projectId, Long appServiceId, String code);

    /**
     * 执行 manul 状态的job
     *
     * @param projectId
     * @param gitlabProjectId
     * @param jobId
     * @param appServiceId
     */
    void playJob(Long projectId, Long gitlabProjectId, Long jobId, Long appServiceId);

    List<DevopsCiJobDTO> listAll();

    String queryMavenSettings(Long projectId, String token, Long id);

    Boolean doesApiTestSuiteRelatedWithPipeline(Long projectId, Long suiteId);

    DevopsCiJobDTO selectByPrimaryKey(Long id);

    DevopsCiJobDTO queryByCiPipelineIdAndName(Long ciPipelineId, String name);

    List<PipelineInstanceReferenceVO> listApiTestTaskReferencePipelineInfo(Long projectId, Set<Long> taskIds);

    List<PipelineInstanceReferenceVO> listPipelineReferenceEnvApp(Long projectId, Long appId);

    List<PipelineInstanceReferenceVO> listChartPipelineReference(Long projectId, Long appId);

    List<PipelineInstanceReferenceVO> listDeployValuePipelineReference(Long projectId, Long valueId);

    List<PipelineInstanceReferenceVO> queryPipelineReferenceHostApp(Long projectId, Long appId);
}
