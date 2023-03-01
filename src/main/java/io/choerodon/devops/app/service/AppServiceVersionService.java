package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.AppServiceVersionAndCommitVO;
import io.choerodon.devops.api.vo.AppServiceVersionRespVO;
import io.choerodon.devops.api.vo.AppServiceVersionVO;
import io.choerodon.devops.api.vo.AppServiceVersionWithHelmConfigVO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/3.
 */
public interface AppServiceVersionService {

    /**
     * 创建应用版本信息
     *
     * @param image            类型
     * @param harborConfigId   harborConfigId
     * @param token            token
     * @param version          版本
     * @param commit           commit
     * @param file             tgz包
     * @param ref              分支名
     * @param gitlabPipelineId
     * @param jobName
     * @param helmRepoId
     * @param gitlabUserId
     */
    void create(String image,
                Long harborConfigId,
                String repoType,
                String token,
                String version,
                String commit,
                MultipartFile file,
                String ref,
                Long gitlabPipelineId,
                String jobName,
                Long helmRepoId,
                Long gitlabUserId);


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
     * @param pageable     分页参数
     * @param version      模糊搜索参数
     * @return ApplicationVersionRespVO
     */
    Page<AppServiceVersionVO> pageByOptions(Long projectId, Long appServiceId, Long appServiceVersionId, Boolean deployOnly, Boolean doPage, String params, PageRequest pageable, String version);

    /**
     * 根据应用id查询需要升级的应用版本
     */
    List<AppServiceVersionRespVO> listUpgradeableAppVersion(Long projectId, Long appServiceServiceId);

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
     * @param pageable
     * @param params
     * @return
     */
    Page<AppServiceVersionRespVO> pageShareVersionByAppId(Long appServiceId, PageRequest pageable, String params);

    /**
     * 获取共享应用版本
     *
     * @param appServiceId
     * @param pageable
     * @param version
     * @return
     */
    Page<AppServiceVersionRespVO> pageShareVersionByAppServiceIdAndVersion(Long appServiceId, PageRequest pageable, String version);

    List<AppServiceVersionDTO> baseListByAppServiceId(Long appServiceId);


    List<AppServiceVersionDTO> baseListAppDeployedVersion(Long projectId, Long appServiceId);

    AppServiceVersionDTO baseQuery(Long appServiceServiceId);

    List<AppServiceVersionDTO> baseListByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId);

    String baseQueryValue(Long versionId);

    AppServiceVersionDTO baseQueryByAppServiceIdAndVersion(Long appServiceId, String version);

    void baseUpdate(AppServiceVersionDTO appServiceVersionDTO);

    List<AppServiceVersionDTO> baseListUpgradeVersion(Long appServiceServiceId);

    void baseCheckByProjectAndVersionId(Long projectId, Long appServiceServiceId);

    List<AppServiceVersionDTO> baseQueryByCommitSha(Long appServiceId, String ref, String sha);

    List<AppServiceVersionDTO> baseListByAppServiceVersionIds(List<Long> appServiceServiceIds);

    List<AppServiceVersionDTO> baseListByAppServiceIdAndBranch(Long appServiceId, String branch);

    String baseQueryByPipelineId(Long pipelineId, String branch, Long appServiceId);

    List<AppServiceVersionDTO> baseListVersions(List<Long> appServiceVersionIds);

    AppServiceVersionDTO baseCreate(AppServiceVersionDTO appServiceVersionDTO);

    AppServiceVersionDTO baseCreateOrUpdate(AppServiceVersionDTO appServiceVersionDTO);

    /**
     * 查询应用服务在组织共享下的最新版本根据服务ID集合
     *
     * @param appServiceIds 应用服务Id集合
     * @param share         是否是组织共享
     * @return 应用服务版本
     */
    List<AppServiceVersionDTO> listServiceVersionByAppServiceIds(Set<Long> appServiceIds, String share, Long projectId, String params);

    /**
     * 根据应用服务IDs和共享规则去查询应用服务的版本列表
     *
     * @param appServiceId
     * @param share
     * @return
     */
    List<AppServiceVersionVO> queryServiceVersionByAppServiceIdAndShare(Long appServiceId, String share);

    List<AppServiceVersionVO> listServiceVersionVoByIds(Set<Long> ids);

    List<AppServiceVersionVO> listVersionById(Long projectId, Long id, String params);

    /**
     * 根据应用服务id和项目Id获取服务版本
     *
     * @param ids
     * @param projectId
     */
    List<AppServiceVersionDTO> listAppServiceVersionByIdsAndProjectId(Set<Long> ids, Long projectId, String params);

    Boolean isVersionUseConfig(Long configId, String configType);

    void deleteByAppServiceId(Long appServiceId);

    /**
     * 批量删除应用服务版本
     *
     * @param projectId
     * @param appServiceId
     * @param versionIds
     */
    void batchDelete(Long projectId, Long appServiceId, Set<Long> versionIds);

    AppServiceVersionDTO queryByCommitShaAndRef(Long appServiceId, String commitSha, String ref);

    AppServiceVersionWithHelmConfigVO queryVersionWithHelmConfig(Long projectId, Long appServiceVersionId);

    /**
     * 发布应用服务版本
     * @param token
     * @param version
     * @param commit
     * @param ref
     * @param gitlabPipelineId
     * @param jobName
     */
    AppServiceVersionDTO publishAppVersion(String token, String version, String commit, String ref, Long gitlabPipelineId, String jobName);

    AppServiceVersionDTO saveAppVersion(String version, String commit, String ref, Long gitlabPipelineId, Long appServiceId);

    /**
     * 保存应用服务版本
     * @param appServiceId 应用服务id
     * @param version 版本名
     * @param commit commit sha
     * @param ref 分支
     * @return
     */
    AppServiceVersionDTO create(Long appServiceId, String version, String commit, String ref);

    List<AppServiceVersionDTO> listAllVersionsWithHelmConfig();

    List<AppServiceVersionDTO> listAllVersionsWithHarborConfig();

    Integer queryCountVersionsWithHelmConfig();

    Integer queryCountVersionsWithHarborConfig();

    Integer queryCountVersionsWithHelmConfigNullOrImageConfigNull();

    List<AppServiceVersionDTO> listAllVersionsWithHelmConfigNullOrImageConfigNull();

    AppServiceVersionDTO queryLatestByAppServiceIdVersionType(Long appServiceId, String version);

    AppServiceVersionDTO saveHelmVersion(Long projectId, String code, String version, String commit, MultipartFile file);
}
