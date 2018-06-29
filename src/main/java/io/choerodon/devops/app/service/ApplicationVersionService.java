package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/3.
 */
public interface ApplicationVersionService {

    /**
     * 分页查询应用版本
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return ApplicationVersionRepDTO
     */
    Page<ApplicationVersionRepDTO> listApplicationVersion(Long projectId, PageRequest pageRequest, String searchParam);

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
    Page<ApplicationVersionRepDTO> listApplicationVersionInApp(Long projectId,
                                                               Long appId,
                                                               PageRequest pageRequest,
                                                               String searchParam);
}
