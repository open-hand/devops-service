package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.devops.infra.common.util.enums.ResourceType;

/**
 * Created by Zenger on 2018/4/12.
 */
public interface ApplicationInstanceService {

    /**
     * 分页查询应用部署
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境Id
     * @param versionId   版本Id
     * @param appId       应用Id
     * @param params      模糊查询参数
     * @return page of devopsEnvPreviewInstanceDTO
     */

    PageInfo<DevopsEnvPreviewInstanceDTO> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                                  Long envId, Long versionId, Long appId, String params);

    /**
     * 查询应用部署
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @return page of ApplicationInstancesDTO
     */
    List<ApplicationInstancesDTO> listApplicationInstances(Long projectId, Long appId);

    /**
     * 查询value列表
     *
     * @param type     部署类型
     * @param instanceId     实例Id
     * @param versionId 版本id
     * @return List
     */
    ReplaceResult queryValues(String type, Long instanceId, Long versionId);


    /**
     * 获取当前部署配置
     *
     * @param instanceId 实例Id
     * @return
     */
    ReplaceResult queryDeployValue(Long instanceId);

    /**
     * 部署应用
     *
     * @param applicationDeployDTO 部署信息
     * @return ApplicationInstanceDTO
     */
    ApplicationInstanceDTO createOrUpdate(ApplicationDeployDTO applicationDeployDTO);

    /**
     * 部署应用,GitOps
     *
     * @param applicationDeployDTO 部署信息
     * @return ApplicationInstanceDTO
     */
    ApplicationInstanceDTO createOrUpdateByGitOps(ApplicationDeployDTO applicationDeployDTO, Long userId);

    /**
     * 查询运行中的实例
     *
     * @param projectId    项目id
     * @param appId        应用id
     * @param appVersionId 应用版本id
     * @param envId        环境id
     * @return list of AppInstanceCodeDTO
     */
    List<AppInstanceCodeDTO> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId);

    /**
     * 环境下某应用运行中或失败的实例
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param envId     环境id
     * @return list of AppInstanceCodeDTO
     */
    List<AppInstanceCodeDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    /**
     * 环境下某应用运行中或失败的实例，判断自动部署是否可替换该实例
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param envId     环境id
     * @return list of AppInstanceCodeDTO
     */
    List<AppInstanceCodeDTO> getByAppIdAndEnvId(Long projectId, Long appId, Long envId);


    /**
     * 实例停止
     *
     * @param instanceId 实例id
     */
    void instanceStop(Long instanceId);

    /**
     * 实例重启
     *
     * @param instanceId 实例id
     */
    void instanceStart(Long instanceId);


    /**
     * 实例重新部署
     *
     * @param instanceId 实例id
     */
    void instanceReStart(Long instanceId);

    /**
     * 实例删除
     *
     * @param instanceId 实例id
     */
    void instanceDelete(Long instanceId);


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
    ReplaceResult queryValue(Long instanceId);

    /**
     * 校验values
     *
     * @param replaceResult values对象
     * @return List
     */
    List<ErrorLineDTO> formatValue(ReplaceResult replaceResult);


    /**
     * 环境总览实例查询
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param params    搜索参数
     * @return DevopsEnvPreviewDTO
     */
    DevopsEnvPreviewDTO listByEnv(Long projectId, Long envId, String params);

    /**
     * 校验实例名唯一性
     *
     * @param instanceName 实例名
     */
    void checkName(String instanceName);

    ReplaceResult getReplaceResult(String versionValue, String deployValue);

    /**
     * 获取升级 Value
     *
     * @param instanceId 实例id
     * @param versionId  版本Id
     * @return ReplaceResult
     */
    ReplaceResult queryUpgradeValue(Long instanceId, Long versionId);

    /**
     * 获取部署时长报表
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param appIds    应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    DeployTimeDTO listDeployTime(Long projectId, Long envId, Long[] appIds, Date startTime, Date endTime);

    /**
     * 获取部署次数报表
     *
     * @param projectId 项目id
     * @param envIds    环境id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    DeployFrequencyDTO listDeployFrequency(Long projectId, Long[] envIds, Long appId, Date startTime, Date endTime);

    /**
     * 获取部署次数报表table
     *
     * @param projectId 项目id
     * @param envIds    环境id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    PageInfo<DeployDetailDTO> pageDeployFrequencyDetail(Long projectId, PageRequest pageRequest, Long[] envIds, Long appId, Date startTime, Date endTime);

    /**
     * 获取部署时长报表table
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param appIds    应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    PageInfo<DeployDetailDTO> pageDeployTimeDetail(Long projectId, PageRequest pageRequest, Long[] appIds, Long envId, Date startTime, Date endTime);

    /**
     * 部署自动化测试应用
     *
     * @param applicationDeployDTO 部署信息
     * @return ApplicationInstanceDTO
     */
    void deployTestApp(ApplicationDeployDTO applicationDeployDTO);

    /**
     * 根据实例id获取更多资源详情(json格式）
     *
     * @param instanceId   实例id
     * @param resourceName 资源(Deployment, DaemonSet, StatefulSet等)的name
     * @param resourceType 资源类型
     * @return 包含json格式的资源详情的DTO
     */
    InstanceControllerDetailDTO getInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType);

    /**
     * 根据实例id获取更多资源详情(yaml格式）
     *
     * @param instanceId   实例id
     * @param resourceName 资源(Deployment, DaemonSet, StatefulSet等)的name
     * @param resourceType 资源类型
     * @return 包含yaml格式的资源详情的DTO
     */
    InstanceControllerDetailDTO getInstanceResourceDetailYaml(Long instanceId, String resourceName, ResourceType resourceType);

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
     * @return ApplicationInstanceDTO
     */
    void operationPodCount(String deploymentName, Long envId, Long count);


    PageInfo<AppInstanceCommandLogDTO> listAppInstanceCommand(PageRequest pageRequest, Long appInstanceId, Date startTime, Date endTime);

}
