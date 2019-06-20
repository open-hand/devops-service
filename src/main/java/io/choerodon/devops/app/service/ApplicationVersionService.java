package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.ApplicationVersionAndCommitDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.api.dto.DeployVersionDTO;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;

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
    List<ApplicationVersionRepDTO> listByAppId(Long appId, Boolean isPublish);

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
    PageInfo<ApplicationVersionRepDTO> listByAppIdAndParamWithPage(Long appId, Boolean isPublish, Long appVersionId, PageRequest pageRequest, String searchParam);

    /**
     * 项目下查询应用所有已部署版本
     *
     * @param projectId 项目ID
     * @param appId     应用ID
     * @return List
     */
    List<ApplicationVersionRepDTO> listDeployedByAppId(Long projectId, Long appId);

    /**
     * 查询部署在某个环境的应用版本
     *
     * @param projectId 项目id
     * @param appId     应用Id
     * @param envId     环境Id
     * @return List
     */
    List<ApplicationVersionRepDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    /**
     * 分页查询某应用下的所有版本
     *
     * @param projectId   项目id
     * @param appId       应用id
     * @param pageRequest 分页参数
     * @param searchParam 模糊搜索参数
     * @return ApplicationVersionRepDTO
     */
    PageInfo<ApplicationVersionRepDTO> listApplicationVersionInApp(Long projectId, Long appId, PageRequest pageRequest,
                                                                   String searchParam);

    /**
     * 根据应用id查询需要升级的应用版本
     */
    List<ApplicationVersionRepDTO> getUpgradeAppVersion(Long projectId, Long appVersionId);

    /**
     * 项目下查询应用最新的版本和各环境下部署的版本
     *
     * @param appId 应用ID
     * @return DeployVersionDTO
     */
    DeployVersionDTO listDeployVersions(Long appId);


    String queryVersionValue(Long appVersionId);

    ApplicationVersionRepDTO queryById(Long appVersionId);

    List<ApplicationVersionRepDTO> listByAppVersionIds(List<Long> appVersionIds);

    List<ApplicationVersionAndCommitDTO> listByAppIdAndBranch(Long appId, String branch);

    Boolean queryByPipelineId(Long pipelineId, String branch, Long appId);

    /**
     * 项目下根据应用Id查询value
     *
     * @param projectId
     * @param appId
     * @return
     */
    String queryValueById(Long projectId, Long appId);

    /**
     * 根据应用和版本号查询应用版本
     *
     * @param appId   应用Id
     * @param version 版本
     * @return ApplicationVersionRepDTO
     */
    ApplicationVersionRepDTO queryByAppAndVersion(Long appId, String version);

    void checkAutoDeploy(ApplicationVersionE versionE);
}
