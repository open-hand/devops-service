package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AppVersionAndValueVO;
import io.choerodon.devops.api.vo.ApplicationVersionAndCommitVO;
import io.choerodon.devops.api.vo.ApplicationVersionRespVO;
import io.choerodon.devops.api.vo.DeployVersionVO;
import io.choerodon.devops.infra.dto.ApplicationLatestVersionDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Zenger on 2018/4/3.
 */
public interface ApplicationVersionService {

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
     * @param appId     应用Id
     * @param isPublish 是否发布
     * @return List
     */
    List<ApplicationVersionRespVO> listByAppId(Long appId, Boolean isPublish);

    /**
     * 根据参数和页数在应用下查询应用所有版本
     *
     * @param appId        应用Id
     * @param appVersionId 应用版本Id
     * @param isPublish    是否发布
     * @param pageRequest  分页参数
     * @param searchParam  查询参数
     * @return List
     */
    PageInfo<ApplicationVersionRespVO> pageByAppIdAndParam(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam);

    /**
     * 项目下查询应用所有已部署版本
     *
     * @param projectId 项目ID
     * @param appId     应用ID
     * @return List
     */
    List<ApplicationVersionRespVO> listDeployedByAppId(Long projectId, Long appId);

    /**
     * 查询部署在某个环境的应用版本
     *
     * @param projectId 项目id
     * @param appId     应用Id
     * @param envId     环境Id
     * @return List
     */
    List<ApplicationVersionRespVO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    /**
     * 分页查询某应用下的所有版本
     *
     * @param projectId   项目id
     * @param appId       应用id
     * @param pageRequest 分页参数
     * @param searchParam 模糊搜索参数
     * @return ApplicationVersionRespVO
     */
    PageInfo<ApplicationVersionRespVO> pageApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest, String searchParam);

    /**
     * 根据应用id查询需要升级的应用版本
     */
    List<ApplicationVersionRespVO> listUpgradeableAppVersion(Long projectId, Long appVersionId);

    /**
     * 项目下查询应用最新的版本和各环境下部署的版本
     *
     * @param appId 应用ID
     * @return DeployVersionVO
     */
    DeployVersionVO queryDeployedVersions(Long appId);


    String queryVersionValue(Long appVersionId);

    ApplicationVersionRespVO queryById(Long appVersionId);

    List<ApplicationVersionRespVO> listByAppVersionIds(List<Long> appVersionIds);

    List<ApplicationVersionAndCommitVO> listByAppIdAndBranch(Long appId, String branch);

    /**
     * 根据pipelineID 查询版本, 判断是否存在
     *
     * @param pipelineId pipeline
     * @param branch     分支
     * @param appId      应用id
     * @return
     */
    Boolean queryByPipelineId(Long pipelineId, String branch, Long appId);

    /**
     * 项目下根据应用Id查询value
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @return
     */
    String queryValueById(Long projectId, Long appId);

    /**
     * 根据应用和版本号查询应用版本
     *
     * @param appId   应用Id
     * @param version 版本
     * @return ApplicationVersionRespVO
     */
    ApplicationVersionRespVO queryByAppAndVersion(Long appId, String version);

    /**
     * 获取远程应用版本
     * @param appId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<ApplicationVersionRespVO> pageVersionByAppId(Long appId, PageRequest pageRequest, String params);

    /**
     * 获取远程应用版本详情
     * @param appId
     * @param versionId
     * @return
     */
    AppVersionAndValueVO queryConfigByVerionId(Long appId, Long versionId);

    List<ApplicationLatestVersionDTO> baseListAppNewestVersion(Long projectId);

    List<ApplicationVersionDTO> baseListByAppId(Long appId, Boolean isPublish);

    PageInfo<ApplicationVersionDTO> basePageByPublished(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam);


    List<ApplicationVersionDTO> baseListAppDeployedVersion(Long projectId, Long appId);

    ApplicationVersionDTO baseQuery(Long appVersionId);

    List<ApplicationVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    String baseQueryValue(Long versionId);

    ApplicationVersionDTO baseQueryByAppIdAndVersion(Long appId, String version);

    void baseUpdatePublishLevelByIds(List<Long> appVersionIds, Long level);

    PageInfo<ApplicationVersionDTO> basePageByOptions(Long projectId, Long appId, PageRequest pageRequest,
                                                      String searchParam, Boolean isProjectOwner,
                                                      Long userId);

    List<ApplicationVersionDTO> baseListByPublished(Long applicationId);

    Boolean baseCheckByAppIdAndVersionIds(Long appId, List<Long> appVersionIds);

    Long baseCreateReadme(String readme);

    String baseQueryReadme(Long readmeValueId);

    void baseUpdate(ApplicationVersionDTO applicationVersionDTO);

    List<ApplicationVersionDTO> baseListUpgradeVersion(Long appVersionId);

    void baseCheckByProjectAndVersionId(Long projectId, Long appVersionId);

    ApplicationVersionDTO baseQueryByCommitSha(Long appId, String ref, String sha);

    ApplicationVersionDTO baseQueryNewestVersion(Long appId);

    List<ApplicationVersionDTO> baseListByAppVersionIds(List<Long> appVersionIds);

    List<ApplicationVersionDTO> baseListByAppIdAndBranch(Long appId, String branch);

    String baseQueryByPipelineId(Long pipelineId, String branch, Long appId);

    String baseQueryValueByAppId(Long appId);

    ApplicationVersionDTO baseCreate(ApplicationVersionDTO applicationVersionDTO);
}
