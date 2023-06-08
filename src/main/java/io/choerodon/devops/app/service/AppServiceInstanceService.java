package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.application.ApplicationInstanceInfoVO;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.eventhandler.payload.BatchDeploymentPayload;
import io.choerodon.devops.app.eventhandler.payload.InstanceSagaPayload;
import io.choerodon.devops.app.eventhandler.payload.MarketInstanceSagaPayload;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/12.
 */
public interface AppServiceInstanceService {
    String INSTANCE_LABEL_RELEASE = "choerodon.io/release";
    String INSTANCE_LABEL_APP_SERVICE_ID = "choerodon.io/app-service-id";
    String PARENT_WORK_LOAD_LABEL = "choerodon.io/parent-workload";
    String PARENT_WORK_LOAD_NAME_LABEL = "choerodon.io/parent-workload-name";

    /**
     * 根据实例id查询实例信息
     *
     * @param instanceId 实例id
     * @return 实例信息
     */
    AppServiceInstanceInfoVO queryInfoById(Long projectId, Long instanceId);


    /**
     * 分页查询环境下实例信息（基本信息）
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param params    查询参数
     * @return 环境下实例基本信息
     */
    Page<AppServiceInstanceInfoVO> pageInstanceInfoByOptions(Long projectId, Long envId, PageRequest pageable, String params);

    /**
     * 分页查询应用部署
     *
     * @param projectId    项目id
     * @param pageable     分页参数
     * @param envId        环境Id
     * @param versionId    版本Id
     * @param appServiceId 应用Id
     * @param params       模糊查询参数
     * @return page of devopsEnvPreviewInstanceDTO
     */

    Page<DevopsEnvPreviewInstanceVO> pageByOptions(Long projectId, PageRequest pageable,
                                                   Long envId, Long versionId, Long appServiceId, Long instanceId, String params);

    /**
     * 查询value列表
     *
     * @param type       部署类型
     * @param instanceId 实例Id
     * @param versionId  版本id
     * @return List
     */
    InstanceValueVO queryDeployValue(String type, Long instanceId, Long versionId);

    /**
     * 部署应用
     *
     * @param projectId          项目id
     * @param appServiceDeployVO 部署信息
     * @param deployType         是否是从流水线发起的部署
     * @return ApplicationInstanceVO
     */
    AppServiceInstanceVO createOrUpdate(@Nullable Long projectId,
                                        AppServiceDeployVO appServiceDeployVO,
                                        DeployType deployType);

    /**
     * 创建或者更新应用市场实例
     *
     * @param projectId                       项目id
     * @param marketInstanceCreationRequestVO 请求参数
     * @return 实例信息
     */
    AppServiceInstanceVO createOrUpdateMarketInstance(Long projectId, MarketInstanceCreationRequestVO marketInstanceCreationRequestVO, Boolean saveRecord);

    /**
     * 部署应用,GitOps
     *
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    AppServiceInstanceVO createOrUpdateByGitOps(AppServiceDeployVO appServiceDeployVO, Long userId);


    /**
     * 部署应用,GitOps
     *
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    AppServiceInstanceVO createOrUpdateMarketInstanceByGitOps(MarketInstanceCreationRequestVO appServiceDeployVO, Long userId);

    /**
     * 查询运行中的实例
     *
     * @param projectId           项目id
     * @param appServiceId        应用id
     * @param appServiceServiceId 应用版本id
     * @param envId               环境id
     * @return baseList of RunningInstanceVO
     */
    List<RunningInstanceVO> listRunningInstance(Long projectId, Long appServiceId, Long appServiceServiceId, Long envId);

    /**
     * 查出通过应用市场部署的实例
     *
     * @param envId 环境id
     * @return
     */
    List<AppServiceInstanceVO> listMarketInstance(Long envId);

    /**
     * 环境下某应用运行中或失败的实例
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @param envId        环境id
     * @return baseList of AppInstanceCodeDTO
     */
    List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);

    /**
     * 实例停止
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     */
    void stopInstance(Long projectId, Long instanceId);

    /**
     * 实例重启
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     */
    void startInstance(Long projectId, Long instanceId);


    /**
     * 实例重新部署
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     */
    DevopsEnvCommandDTO restartInstance(Long projectId, Long instanceId, DeployType deployType, Boolean saveRecord);

    /**
     * 实例删除
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     */
    void deleteInstance(@Nullable Long projectId, Long instanceId, Boolean deletePrmotheus);


    /**
     * 实例删除
     *
     * @param instanceId 实例id
     */
    void instanceDeleteByGitOps(Long instanceId);


    /**
     * 获取最新部署配置
     *
     * @param instanceId 实例id
     * @return string
     */
    InstanceValueVO queryLastDeployValue(Long instanceId);

    /**
     * 校验values
     *
     * @param instanceValueVO values对象
     * @return List
     */
    List<ErrorLineVO> formatValue(InstanceValueVO instanceValueVO);


    /**
     * 校验实例名唯一性
     *
     * @param instanceName 实例名
     * @param envId        环境Id
     */
    void checkName(String instanceName, Long envId);

    /**
     * 判断实例名唯一性
     *
     * @param instanceName 实例名
     * @param envId        环境Id
     * @return true表示通过
     */
    boolean isNameValid(String instanceName, Long envId);

    /**
     * @param versionValue
     * @param deployValue
     * @return
     */
    InstanceValueVO getReplaceResult(String versionValue, String deployValue);

    /**
     * 获取升级 Value
     *
     * @param instanceId 实例id
     * @param versionId  版本Id
     * @return InstanceValueVO
     */
    InstanceValueVO queryUpgradeValue(Long instanceId, Long versionId);

    /**
     * 获取升级 Value
     *
     * @param instanceId           实例id
     * @param marketDeployObjectId 市场发布对象id
     * @return InstanceValueVO
     */
    InstanceValueVO queryUpgradeValueForMarketInstance(Long projectId, Long instanceId, Long marketDeployObjectId);

    /**
     * 获取部署时长报表
     *
     * @param projectId     项目id
     * @param envId         环境id
     * @param appServiceIds 应用id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return List
     */
    DeployTimeVO listDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date endTime);

    /**
     * 获取部署次数报表
     *
     * @param projectId    项目id
     * @param envIds       环境id
     * @param appServiceId 应用id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return List
     */
    DeployFrequencyVO listDeployFrequency(Long projectId, Long[] envIds, Long appServiceId, Date startTime, Date endTime);

    /**
     * 获取部署次数报表table
     *
     * @param projectId    项目id
     * @param envIds       环境id
     * @param appServiceId 应用id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return Page
     */
    Page<DeployDetailTableVO> pageDeployFrequencyTable(Long projectId, PageRequest pageable, Long[] envIds, Long appServiceId, Date startTime, Date endTime);

    /**
     * 获取部署时长报表table
     *
     * @param projectId     项目id
     * @param envId         环境id
     * @param appServiceIds 应用id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return List
     */
    Page<DeployDetailTableVO> pageDeployTimeTable(Long projectId, PageRequest pageable, Long[] appServiceIds, Long envId, Date startTime, Date endTime);

    /**
     * 根据实例id获取更多资源详情(json格式）
     *
     * @param instanceId   实例id
     * @param resourceName 资源(Deployment, DaemonSet, StatefulSet等)的name
     * @param resourceType 资源类型
     * @return 包含json格式的资源详情的DTO
     */
    InstanceControllerDetailVO queryInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType);

    /**
     * 根据实例id获取更多资源详情(yaml格式）
     *
     * @param instanceId   实例id
     * @param resourceName 资源(Deployment, DaemonSet, StatefulSet等)的name
     * @param resourceType 资源类型
     * @return 包含yaml格式的资源详情的DTO
     */
    InstanceControllerDetailVO getInstanceResourceDetailYaml(Long instanceId, String resourceName, ResourceType resourceType);


    /**
     * 操作pod的数量
     *
     * @param projectId  项目id
     * @param envId      环境id
     * @param kind       资源类型
     * @param instanceId 实例id
     * @param name       deploymentName
     * @param count      pod数量
     * @param workload   是否为操作工作负载pod
     */
    void operationPodCount(Long projectId, String kind, Long instanceId, String name, Long envId, Long count, boolean workload);


    DevopsEnvResourceVO listResourcesInHelmRelease(Long instanceId);

    /**
     * 获取预览 Value
     *
     * @param instanceValueVO     yaml
     * @param appServiceServiceId 版本Id
     * @return InstanceValueVO
     */
    InstanceValueVO queryPreviewValues(InstanceValueVO instanceValueVO, Long appServiceServiceId);

    /**
     * @param instanceSagaPayload
     */
    void createInstanceBySaga(InstanceSagaPayload instanceSagaPayload);

    void createMarketInstanceBySaga(MarketInstanceSagaPayload marketInstanceSagaPayload);

    /**
     * @param commandId
     * @return
     */
    AppServiceInstanceRepVO queryByCommandId(Long commandId);

    List<AppServiceInstanceDTO> baseListByAppId(Long appServiceId);

    List<AppServiceInstanceDTO> baseListByValueId(Long valueId);

    AppServiceInstanceDTO baseQuery(Long id);

    List<AppServiceInstanceDTO> baseListByEnvId(Long envId);

    void deleteByEnvId(Long envId);

    AppServiceInstanceDTO baseQueryByCodeAndEnv(String code, Long envId);

    AppServiceInstanceDTO baseCreate(AppServiceInstanceDTO appServiceInstanceDTO);

    void baseUpdate(AppServiceInstanceDTO appServiceInstanceDTO);

    List<AppServiceInstanceOverViewDTO> baseListApplicationInstanceOverView(Long projectId, Long appServiceId, List<Long> envIds);

    String baseQueryValueByInstanceId(Long instanceId);

    List<DeployDTO> baseListDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date endTime);

    List<DeployDTO> baseListDeployFrequency(Long projectId, Long[] envIds, Long appServiceId,
                                            Date startTime, Date endTime);

    Page<DeployDTO> basePageDeployFrequencyTable(Long projectId, PageRequest pageable, Long[] envIds, Long appServiceId,
                                                 Date startTime, Date endTime);

    Page<DeployDTO> basePageDeployTimeTable(Long projectId, PageRequest pageable, Long envId, Long[] appServiceIds,
                                            Date startTime, Date endTime);

    String baseGetInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType);

    void updateStatus(AppServiceInstanceDTO appServiceInstanceDTO);

    Integer countByOptions(Long envId, String status, Long appServiceId);

    /**
     * 批量部署实例（及其网络、域名）
     *
     * @param appServiceDeployVOS 批量部署信息
     * @return 返回部署的实例信息
     */
    List<AppServiceInstanceVO> batchDeployment(Long projectId, List<AppServiceDeployVO> appServiceDeployVOS);

    /**
     * 处理批量部署事件
     *
     * @param batchDeploymentPayload 批量部署信息
     */
    void batchDeploymentSaga(BatchDeploymentPayload batchDeploymentPayload);

    /**
     * 查询实例的版本
     *
     * @param appServiceInstanceId 实例的id
     * @return 版本
     */
    AppServiceVersionDTO queryVersion(Long appServiceInstanceId);

    /**
     * 查询应用服务在环境下的实例列表
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param envId        环境id
     * @param withPodInfo  是否设置pod信息
     * @return 实例列表
     */
    List<ApplicationInstanceInfoVO> listByServiceAndEnv(Long projectId, Long appServiceId, Long envId, boolean withPodInfo);

//    void hzeroDeploy(Long detailsRecordId);

//    void pipelineDeployHzeroApp(Long projectId, DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO);

    /**
     * 通过code和envId查询AppServiceInstanceDTO的code集合
     *
     * @param codes
     * @param envId
     * @return code列表
     */
    List<AppServiceInstanceDTO> listInstanceByDeployDetailsCode(List<String> codes, Long envId);

    /**
     * 通过code和envId查询AppServiceInstanceDTO的status
     *
     * @param code
     * @param envId
     * @return 状态
     */
    String queryInstanceStatusByEnvIdAndCode(String code, Long envId);

    /**
     * 查询应用实例数量
     */
    Integer countInstance();

    List<AppServiceInstanceDTO> listInstances();

    String getSecret(AppServiceDTO appServiceDTO, Long appServiceVersionId, DevopsEnvironmentDTO
            devopsEnvironmentDTO);

    String makeMarketSecret(Long projectId, DevopsEnvironmentDTO
            devopsEnvironmentDTO, MarketServiceDeployObjectVO marketServiceDeployObjectVO);

    InstanceValueVO queryValues(Long instanceId);

    InstanceValueVO queryValueForMarketInstance(Long projectId, Long instanceId, Long marketDeployObjectId);

    List<PipelineInstanceReferenceVO> queryInstancePipelineReference(Long projectId, Long instanceId);

    Boolean isInstanceDeploying(Long instanceId);

    void deleteHelmHookJob(Long projectId, Long instanceId, Long envId, Long commandId, String jobName);

    void syncValueToDeploy(Long projectId, AppServiceSyncValueDeployVO syncValueDeployVO);

    List<AppServiceInstanceVO> listInstanceByValueId(Long projectId, Long valueId, String params);

    boolean setImagePullSecrets(Integer userId, DevopsEnvironmentDTO devopsEnvironmentDTO, Map<String, C7nHelmRelease> c7nHelmReleases, String gitopsRepoPath, String commitSha);
}
