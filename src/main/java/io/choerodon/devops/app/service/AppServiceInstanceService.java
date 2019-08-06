package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.app.eventhandler.payload.InstanceSagaPayload;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceOverViewDTO;
import io.choerodon.devops.infra.dto.DeployDTO;
import io.choerodon.devops.infra.enums.ResourceType;

/**
 * Created by Zenger on 2018/4/12.
 */
public interface AppServiceInstanceService {
    /**
     * 根据实例id查询实例信息
     *
     * @param instanceId 实例id
     * @return 实例信息
     */
    AppServiceInstanceInfoVO queryInfoById(Long instanceId);


    /**
     * 分页查询环境下实例信息（基本信息）
     *
     * @param projectId 项目id
     * @param envId 环境id
     * @param params 查询参数
     * @return 环境下实例基本信息
     */
    PageInfo<AppServiceInstanceInfoVO> pageInstanceInfoByOptions(Long projectId, Long envId, PageRequest pageRequest, String params);

    /**
     * 分页查询应用部署
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境Id
     * @param versionId   版本Id
     * @param appServiceId       应用Id
     * @param params      模糊查询参数
     * @return page of devopsEnvPreviewInstanceDTO
     */

    PageInfo<DevopsEnvPreviewInstanceVO> pageByOptions(Long projectId, PageRequest pageRequest,
                                                       Long envId, Long versionId, Long appServiceId, Long instanceId, String params);

    /**
     * 查询应用部署
     *
     * @param projectId 项目id
     * @param appServiceId     应用id
     * @return page of ApplicationInstanceOverViewVO
     */
    List<AppServiceInstanceOverViewVO> listApplicationInstanceOverView(Long projectId, Long appServiceId);

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
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    AppServiceInstanceVO createOrUpdate(AppServiceDeployVO appServiceDeployVO);

    /**
     * 部署应用,GitOps
     *
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    AppServiceInstanceVO createOrUpdateByGitOps(AppServiceDeployVO appServiceDeployVO, Long userId);

    /**
     * 查询运行中的实例
     *
     * @param projectId    项目id
     * @param appServiceId        应用id
     * @param appServiceServiceId 应用版本id
     * @param envId        环境id
     * @return baseList of RunningInstanceVO
     */
    List<RunningInstanceVO> listRunningInstance(Long projectId, Long appServiceId, Long appServiceServiceId, Long envId);

    /**
     * 环境下某应用运行中或失败的实例
     *
     * @param projectId 项目id
     * @param appServiceId     应用id
     * @param envId     环境id
     * @return baseList of AppInstanceCodeDTO
     */
    List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);

    /**
     * 实例停止
     *
     * @param instanceId 实例id
     */
    void stopInstance(Long instanceId);

    /**
     * 实例重启
     *
     * @param instanceId 实例id
     */
    void startInstance(Long instanceId);


    /**
     * 实例重新部署
     *
     * @param instanceId 实例id
     */
    void restartInstance(Long instanceId);

    /**
     * 实例删除
     *
     * @param instanceId 实例id
     */
    void deleteInstance(Long instanceId);


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
     * 环境总览实例查询
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param params    搜索参数
     * @return DevopsEnvPreviewVO
     */
    DevopsEnvPreviewVO listByEnv(Long projectId, Long envId, String params);

    /**
     * 校验实例名唯一性
     *
     * @param instanceName 实例名
     * @param envId        环境Id
     */
    void checkName(String instanceName, Long envId);

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
     * 获取部署时长报表
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param appServiceIds    应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    DeployTimeVO listDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date endTime);

    /**
     * 获取部署次数报表
     *
     * @param projectId 项目id
     * @param envIds    环境id
     * @param appServiceId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    DeployFrequencyVO listDeployFrequency(Long projectId, Long[] envIds, Long appServiceId, Date startTime, Date endTime);

    /**
     * 获取部署次数报表table
     *
     * @param projectId 项目id
     * @param envIds    环境id
     * @param appServiceId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return Page
     */
    PageInfo<DeployDetailTableVO> pageDeployFrequencyTable(Long projectId, PageRequest pageRequest, Long[] envIds, Long appServiceId, Date startTime, Date endTime);

    /**
     * 获取部署时长报表table
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param appServiceIds    应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    PageInfo<DeployDetailTableVO> pageDeployTimeTable(Long projectId, PageRequest pageRequest, Long[] appServiceIds, Long envId, Date startTime, Date endTime);

    /**
     * 部署自动化测试应用
     *
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    void deployTestApp(AppServiceDeployVO appServiceDeployVO);

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
     * 查询自动化测试应用实例状态
     *
     * @param testReleases
     */
    void getTestAppStatus(Map<Long, List<String>> testReleases);


    /**
     * 操作pod的数量
     *
     * @param envId          环境id
     * @param deploymentName deploymentName
     * @param count          pod数量
     * @return ApplicationInstanceVO
     */
    void operationPodCount(String deploymentName, Long envId, Long count);


    DevopsEnvResourceVO listResourcesInHelmRelease(Long instanceId);

    /**
     * 获取预览 Value
     *
     * @param instanceValueVO yaml
     * @param appServiceServiceId    版本Id
     * @return InstanceValueVO
     */
    InstanceValueVO queryPreviewValues(InstanceValueVO instanceValueVO, Long appServiceServiceId);

    /**
     * 部署远程应用
     *
     * @param appRemoteDeployDTO
     * @return
     */
    AppServiceInstanceVO deployRemoteApp(Long projectId, AppServiceRemoteDeployVO appRemoteDeployDTO);


    /**
     * @param instanceSagaPayload
     */
    void createInstanceBySaga(InstanceSagaPayload instanceSagaPayload);

    /**
     * @param commandId
     * @return
     */
    AppServiceInstanceRepVO queryByCommandId(Long commandId);

    List<AppServiceInstanceDTO> baseListByAppId(Long appServiceId);

    List<AppServiceInstanceDTO> baseList();

    List<AppServiceInstanceDTO> baseListByValueId(Long valueId);

    AppServiceInstanceDTO baseQuery(Long id);

    List<AppServiceInstanceDTO> baseListByEnvId(Long envId);

    void deleteByEnvId(Long envId);

    AppServiceInstanceDTO baseQueryByCodeAndEnv(String code, Long envId);

    AppServiceInstanceDTO baseCreate(AppServiceInstanceDTO appServiceInstanceDTO);

    List<AppServiceInstanceDTO> baseListByOptions(Long projectId, Long appServiceId, Long appServiceServiceId, Long envId);

    List<AppServiceInstanceDTO> baseListByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);

    int baseCountByOptions(Long envId, Long appServiceId, String appServiceInstanceCode);

    String baseQueryValueByEnvIdAndAppId(Long envId, Long appServiceId);

    void baseUpdate(AppServiceInstanceDTO appServiceInstanceDTO);

    List<AppServiceInstanceOverViewDTO> baseListApplicationInstanceOverView(Long projectId, Long appServiceId, List<Long> envIds);

    String baseQueryValueByInstanceId(Long instanceId);

    void baseDelete(Long id);

    List<DeployDTO> baseListDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date endTime);

    List<DeployDTO> baselistDeployFrequency(Long projectId, Long[] envIds, Long appServiceId,
                                            Date startTime, Date endTime);

    PageInfo<DeployDTO> basePageDeployFrequencyTable(Long projectId, PageRequest pageRequest, Long[] envIds, Long appServiceId,
                                                     Date startTime, Date endTime);

    PageInfo<DeployDTO> basePageDeployTimeTable(Long projectId, PageRequest pageRequest, Long envId, Long[] appServiceIds,
                                                Date startTime, Date endTime);

    void baseCheckName(String instanceName, Long envId);

    String baseGetInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType);

    void baseDeleteInstanceRelInfo(Long instanceId);

}
