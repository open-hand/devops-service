package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.*;
=======
import io.choerodon.devops.api.vo.AppUserPermissionRepDTO;
import io.choerodon.devops.api.vo.ApplicationCodeDTO;
import io.choerodon.devops.api.vo.ApplicationImportDTO;
import io.choerodon.devops.api.vo.ApplicationRepVO;
import io.choerodon.devops.api.vo.ApplicationReqVO;
import io.choerodon.devops.api.vo.ApplicationTemplateRespVO;
import io.choerodon.devops.api.vo.ApplicationUpdateVO;
import io.choerodon.devops.api.vo.SonarContentsDTO;
import io.choerodon.devops.api.vo.SonarTableDTO;
>>>>>>> [REF] refactor ApplicationTemplateController
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppPayload;
import io.choerodon.devops.app.eventhandler.payload.IamAppPayLoad;
import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.devops.infra.enums.GitPlatformType;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationService {

    /**
     * 项目下创建应用
     *
     * @param projectId         项目Id
     * @param applicationReqDTO 应用信息
     * @return ApplicationTemplateDTO
     */
    ApplicationRepVO create(Long projectId, ApplicationReqVO applicationReqDTO);


    /**
     * 项目下查询单个应用信息
     *
     * @param projectId     项目id
     * @param applicationId 应用Id
     * @return ApplicationRepDTO
     */
    ApplicationRepVO query(Long projectId, Long applicationId);

    /**
     * 项目下删除创建失败应用
     *
     * @param projectId     项目id
     * @param applicationId 应用Id
     */
    void delete(Long projectId, Long applicationId);

    /**
     * 项目下更新应用信息
     *
     * @param projectId            项目id
     * @param applicationUpdateDTO 应用信息
     * @return Boolean
     */
    Boolean update(Long projectId, ApplicationUpdateVO applicationUpdateDTO);


    /**
     * 项目下启用停用应用信息
     *
     * @param applicationId 应用id
     * @param active        启用停用
     * @return Boolean
     */
    Boolean updateActive(Long applicationId, Boolean active);

    /**
     * 组织下分页查询应用
     *
     * @param projectId   项目id
     * @param isActive    是否启用
     * @param appMarket   应用市场导入
     * @param hasVersion  是否存在版本
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    PageInfo<ApplicationRepVO> pageByOptions(Long projectId,
                                             Boolean isActive,
                                             Boolean hasVersion,
                                             Boolean appMarket,
                                             String type,
                                             Boolean doPage,
                                             PageRequest pageRequest,
                                             String params);

    /**
     * 组织下分页查询应用 远程应用分享专用
     *
     * @param projectId
     * @param isActive
     * @param hasVersion
     * @param doPage
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<ApplicationRepDTO> listByOptions(Long projectId,
                                              Boolean isActive,
                                              Boolean hasVersion,
                                              Boolean doPage,
                                              PageRequest pageRequest,
                                              String params);


    /**
     * 处理应用创建逻辑
     *
     * @param gitlabProjectEventDTO 应用信息
     */
    void operationApplication(DevOpsAppPayload gitlabProjectEventDTO);

    /**
     * 处理应用导入逻辑
     *
     * @param devOpsAppImportPayload 应用导入相关信息
     */
    void operationApplicationImport(DevOpsAppImportPayload devOpsAppImportPayload);


    /**
     * 设置应用创建失败状态
     *
     * @param gitlabProjectEventDTO 应用信息
     * @param projectId             可为空
     */
    void setAppErrStatus(String gitlabProjectEventDTO, Long projectId);

    /**
     * 项目下应用查询ci脚本文件
     *
     * @param token token
     * @return File
     */
    String queryFile(String token, String type);

    /**
     * 根据环境id获取已部署正在运行实例的应用
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param status    环境状态
     * @return baseList of ApplicationRepDTO
     */
    List<ApplicationCodeDTO> listByEnvId(Long projectId, Long envId, String status, Long appId);

    /**
     * 根据环境id获取已部署正在运行实例的应用
     *
     * @param projectId   项目id
     * @param envId       环境Id
     * @param pageRequest 分页参数
     * @return baseList of ApplicationRepDTO
     */
    PageInfo<ApplicationCodeDTO> pageByIds(Long projectId, Long envId, Long appId, PageRequest pageRequest);

    /**
     * 项目下查询所有已经启用的应用
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<ApplicationRepVO> listByActive(Long projectId);

    /**
     * 项目下查询所有可选已经启用的应用
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<ApplicationRepVO> listAll(Long projectId);

    /**
     * 创建应用校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      应用name
     */
    void checkName(Long projectId, String name);

    /**
     * 创建应用校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      应用code
     */
    void checkCode(Long projectId, String code);

    /**
     * 查询应用模板
     *
     * @param projectId    项目ID
     * @param isPredefined 是否只查询预定义模板
     * @return Page
     */
    List<ApplicationTemplateRespVO> listTemplate(Long projectId, Boolean isPredefined);

    /**
     * 项目下查询已经启用有版本未发布的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return baseList of ApplicationRepDTO
     */
    PageInfo<ApplicationReqVO> pageByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下分页查询代码仓库
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return page of ApplicationRepDTO
     */
    PageInfo<ApplicationRepVO> pageCodeRepository(Long projectId, PageRequest pageRequest, String params);

    /**
     * 获取应用下所有用户权限
     *
     * @param appId 应用id
     * @return List
     */
    List<AppUserPermissionRepDTO> listAllUserPermission(Long appId);

    /**
     * valid the repository url and access token
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl   repository url
     * @param accessToken     access token (Nullable)
     * @return true if valid
     */
    Boolean validateRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken);

    /**
     * 从外部代码托管平台导入项目创建应用
     *
     * @param projectId            project id
     * @param applicationImportDTO 导入操作的相关信息
     * @return response
     */
    ApplicationRepVO importApp(Long projectId, ApplicationImportDTO applicationImportDTO);


    /**
     * 根据应用code查询应用
     *
     * @param projectId 项目Id
     * @param code      应用code
     * @return ApplicationRepDTO
     */
    ApplicationRepVO queryByCode(Long projectId, String code);


    /**
     * 处理iam服务创建应用
     *
     * @param iamAppPayLoad 应用相关信息
     */
    void createIamApplication(IamAppPayLoad iamAppPayLoad);


    /**
     * 处理iam服务更新应用
     *
     * @param iamAppPayLoad 应用相关信息
     */
    void updateIamApplication(IamAppPayLoad iamAppPayLoad);

    /**
     * 处理iam服务删除应用
     *
     * @param iamAppPayLoad 应用相关信息
     */
    void deleteIamApplication(IamAppPayLoad iamAppPayLoad);

    /**
     * 校验harbor配置信息是否正确
     *
     * @param url      harbor地址
     * @param userName harbor用户名
     * @param password harbor密码
     * @param project  harbor项目
     * @param email    harbor邮箱
     * @return Boolean
     */
    Boolean checkHarbor(String url, String userName, String password, String project, String email);

    /**
     * 校验chart配置信息是否正确
     *
     * @param url chartmusume地址
     * @return Boolean
     */
    Boolean checkChart(String url);

    /**
     * 查看sonarqube相关信息
     *
     * @param projectId 项目Id
     * @param appId     应用id
     * @return
     */
    SonarContentsDTO getSonarContent(Long projectId, Long appId);

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId 项目Id
     * @param appId     应用id
     * @return
     */
    SonarTableDTO getSonarTable(Long projectId, Long appId, String type, Date startTime, Date endTime);


    /**
     * 或者gitlab地址
     *
     * @param projectId
     * @param appId
     * @return
     */
    String getGitlabUrl(Long projectId, Long appId);


    void baseCheckApp(Long projectId, Long appId);

    int baseUpdate(ApplicationDTO applicationDTO);

    void updateApplicationStatus(ApplicationDTO applicationDTO);

    ApplicationDTO baseQuery(Long applicationId);

    PageInfo<ApplicationDTO> basePageByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean
            appMarket,
                                                      String type, Boolean doPage, PageRequest pageRequest, String params);

    PageInfo<ApplicationDTO> basePageCodeRepository(Long projectId, PageRequest pageRequest, String params,
                                                           Boolean isProjectOwner, Long userId);


    ApplicationDTO baseQueryByCode(String code, Long projectId);

    ApplicationDTO baseQueryByCodeWithNullProject(String code);

    List<ApplicationDTO> baseListByEnvId(Long projectId, Long envId, String status);

    PageInfo<ApplicationDTO> basePageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest);

    List<ApplicationDTO> baseListByActive(Long projectId);

    List<ApplicationDTO> baseListDeployedApp(Long projectId);

    PageInfo<ApplicationDTO> basePageByActiveAndPubAndHasVersion(Long projectId, Boolean isActive,
                                                                        PageRequest pageRequest, String params);

    ApplicationDTO baseQueryByToken(String token);

    void baseCheckAppCanDisable(Long applicationId);

    List<ApplicationDTO> baseListByCode(String code);

    List<ApplicationDTO> baseListByGitLabProjectIds(List<Long> gitLabProjectIds);

    void baseDelete(Long appId);

    List<ApplicationDTO> baseListByProjectIdAndSkipCheck(Long projectId);

    List<ApplicationDTO> baseListByProjectId(Long projectId);

    void baseUpdateHarborConfig(Long projectId, Long newConfigId, Long oldConfigId, boolean harborPrivate);

    ApplicationDTO getApplicationDTO(Long projectId, ApplicationReqVO applicationReqDTO);

    void baseCheckName(Long projectId, String appName);

    void baseCheckCode(ApplicationDTO applicationDTO);

    ApplicationDTO baseCreate(ApplicationDTO applicationDTO);
}
