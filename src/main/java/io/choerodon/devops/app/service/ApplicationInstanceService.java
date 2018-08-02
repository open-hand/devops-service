package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
     * @return page of applicationInstanceDTO
     */
    Page<ApplicationInstanceDTO> listApplicationInstance(Long projectId, PageRequest pageRequest,
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
     * @param appId     应用id
     * @param envId     环境id
     * @param versionId 版本id
     * @return List
     */
    ReplaceResult queryValues(Long appId, Long envId, Long versionId);

    /**
     * 部署应用
     *
     * @param applicationDeployDTO 部署信息
     * @return ApplicationInstanceDTO
     */
    ApplicationInstanceDTO create(ApplicationDeployDTO applicationDeployDTO, boolean gitops);

    /**
     * 获取版本特性
     *
     * @param appInstanceId 实例id
     * @return list of versionFeaturesDTO
     */
    List<VersionFeaturesDTO> queryVersionFeatures(Long appInstanceId);

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
     * 实例删除
     *
     * @param instanceId 实例id
     */
    void instanceDelete(Long instanceId, boolean gitops);

    /**
     * 实例回滚
     *
     * @param version    版本
     * @param instanceId 实例id
     */
    void instanceRollback(Integer version, Long instanceId);

    /**
     * 获取部署 Value
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
     * 获取预览 Value
     *
     * @param replaceResult yaml
     * @param appVersionId  版本Id
     * @return ReplaceResult
     */
    ReplaceResult previewValues(ReplaceResult replaceResult, Long appVersionId);
}
