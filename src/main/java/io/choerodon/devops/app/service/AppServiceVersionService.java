package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.AppServiceLatestVersionDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Zenger on 2018/4/3.
 */
public interface AppServiceVersionService {

    /**
     * 创建应用版本信息
     *
     * @param token   token
     * @param image   类型
     * @param version 版本
     * @param commit  commit
     * @param file    tgz包
     */
    void create(String image, String token, String version, String commit, MultipartFile file);

    /**
     * 应用下查询应用所有版本
     *
     * @param appServiceId 应用Id
     * @return List
     */
    List<AppServiceVersionRespVO> listByAppServiceId(Long appServiceId);

    /**
     * 根据参数和页数在应用下查询应用所有版本
     *
     * @param appServiceId        应用Id
     * @param appServiceServiceId 应用版本Id
     * @param isPublish           是否发布
     * @param pageRequest         分页参数
     * @param searchParam         查询参数
     * @return List
     */
    PageInfo<AppServiceVersionRespVO> pageByAppIdAndParam(Long appServiceId, Boolean isPublish, Long appServiceServiceId, PageRequest pageRequest, String searchParam);

    /**
     * 项目下查询应用所有已部署版本
     *
     * @param projectId    项目ID
     * @param appServiceId 应用ID
     * @return List
     */
    List<AppServiceVersionRespVO> listDeployedByAppId(Long projectId, Long appServiceId);

    /**
     * 查询部署在某个环境的应用版本
     *
     * @param projectId    项目id
     * @param appServiceId 应用Id
     * @param envId        环境Id
     * @return List
     */
    List<AppServiceVersionRespVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);

    /**
     * 分页查询某应用下的所有版本
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @param pageRequest  分页参数
     * @param searchParam  模糊搜索参数
     * @return ApplicationVersionRespVO
     */
    PageInfo<AppServiceVersionVO> pageByOptions(Long projectId, Long appServiceId, PageRequest pageRequest, String searchParam);

    /**
     * 根据应用id查询需要升级的应用版本
     */
    List<AppServiceVersionRespVO> listUpgradeableAppVersion(Long projectId, Long appServiceServiceId);

    /**
     * 项目下查询应用最新的版本和各环境下部署的版本
     *
     * @param appServiceId 应用ID
     * @return DeployVersionVO
     */
    DeployVersionVO queryDeployedVersions(Long appServiceId);


    String queryVersionValue(Long appServiceServiceId);

    AppServiceVersionRespVO queryById(Long appServiceServiceId);

    List<AppServiceVersionRespVO> listByAppServiceVersionIds(List<Long> appServiceServiceIds);

    List<AppServiceVersionAndCommitVO> listByAppIdAndBranch(Long appServiceId, String branch);

    /**
     * 根据pipelineID 查询版本, 判断是否存在
     *
     * @param pipelineId   pipeline
     * @param branch       分支
     * @param appServiceId 应用id
     * @return
     */
    Boolean queryByPipelineId(Long pipelineId, String branch, Long appServiceId);

    /**
     * 项目下根据应用Id查询value
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @return
     */
    String queryValueById(Long projectId, Long appServiceId);

    /**
     * 根据应用和版本号查询应用版本
     *
     * @param appServiceId 应用Id
     * @param version      版本
     * @return ApplicationVersionRespVO
     */
    AppServiceVersionRespVO queryByAppAndVersion(Long appServiceId, String version);

    /**
     * 获取共享应用版本
     *
     * @param appServiceId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, PageRequest pageRequest, String params);


    List<AppServiceLatestVersionDTO> baseListAppNewestVersion(Long projectId);

    List<AppServiceVersionDTO> baseListByAppServiceId(Long appServiceId);

    PageInfo<AppServiceVersionDTO> basePageByPublished(Long appServiceId, Boolean isPublish, Long appServiceServiceId, PageRequest pageRequest, String searchParam);


    List<AppServiceVersionDTO> baseListAppDeployedVersion(Long projectId, Long appServiceId);

    AppServiceVersionDTO baseQuery(Long appServiceServiceId);

    List<AppServiceVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);

    String baseQueryValue(Long versionId);

    AppServiceVersionDTO baseQueryByAppIdAndVersion(Long appServiceId, String version);

    void baseUpdatePublishLevelByIds(List<Long> appServiceServiceIds, Long level);

    PageInfo<AppServiceVersionDTO> basePageByOptions(Long projectId, Long appServiceId, PageRequest pageRequest,
                                                     String searchParam, Boolean isProjectOwner,
                                                     Long userId);

    List<AppServiceVersionDTO> baseListByPublished(Long applicationId);

    Boolean baseCheckByAppIdAndVersionIds(Long appServiceId, List<Long> appServiceServiceIds);

    Long baseCreateReadme(String readme);

    String baseQueryReadme(Long readmeValueId);

    void baseUpdate(AppServiceVersionDTO appServiceVersionDTO);

    List<AppServiceVersionDTO> baseListUpgradeVersion(Long appServiceServiceId);

    void baseCheckByProjectAndVersionId(Long projectId, Long appServiceServiceId);

    AppServiceVersionDTO baseQueryByCommitSha(Long appServiceId, String ref, String sha);

    AppServiceVersionDTO baseQueryNewestVersion(Long appServiceId);

    List<AppServiceVersionDTO> baseListByAppVersionIds(List<Long> appServiceServiceIds);

    List<AppServiceVersionDTO> baseListByAppIdAndBranch(Long appServiceId, String branch);

    String baseQueryByPipelineId(Long pipelineId, String branch, Long appServiceId);

    String baseQueryValueByAppId(Long appServiceId);

    AppServiceVersionDTO baseCreate(AppServiceVersionDTO appServiceVersionDTO);

    /**
     * 查询应用服务在组织共享下的最新版本
     *
     * @param appServiceId 应用服务Id
     * @return 应用服务版本
     */
    AppServiceVersionDTO queryServiceVersionByAppServiceId(Long appServiceId, String share);
}
