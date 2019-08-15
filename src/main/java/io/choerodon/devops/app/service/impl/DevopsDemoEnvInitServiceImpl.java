package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.MockMultipartFile;
import io.choerodon.devops.app.eventhandler.DevopsSagaHandler;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.app.eventhandler.payload.OrganizationRegisterEventPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.gitlab.MergeRequestDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

/**
 * 为搭建Demo环境初始化项目中的一些数据，包含应用，分支，提交，版本，应用市场等
 * 目前实现不选择程序中进行循环等待等待ci生成版本，而是手动生成版本，但是该版本可以看
 * 但是会部署失败，显示找不到Chart.
 *
 * @author zmf
 */
@Service
public class DevopsDemoEnvInitServiceImpl implements DevopsDemoEnvInitService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${demo.data.file.path:demo/demo-data.json}")
    private String demoDataFilePath;
    @Value("${demo.tgz.file.path:demo/code-i.tgz}")
    private String tgzFilePath;

    @Autowired
    private DevopsConfigService projectConfigService;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private AppServiceUserPermissionService appServiceUserPermissionService;
    @Autowired
    private DevopsSagaHandler devopsSagaHandler;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    private Gson gson = new Gson();

    private DemoDataVO demoDataVO;
    private Integer gitlabUserId;

    @PostConstruct
    public void loadData() {
        try {
            String content = StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream(demoDataFilePath), Charset.forName("UTF-8"));
            demoDataVO = JSONObject.parseObject(content, DemoDataVO.class);
        } catch (Exception e) {
            logger.error("Load content of demo data failed. exception is: {}", e);
        }
    }

    @Override
    public void initialDemoEnv(OrganizationRegisterEventPayload organizationRegisterEventPayload) {
        Long projectId = organizationRegisterEventPayload.getProject().getId();
        // 1. 创建应用
        AppServiceReqVO app = demoDataVO.getApplicationInfo();
        app.setIsSkipCheckPermission(Boolean.TRUE);

        AppServiceRepVO applicationRepDTO = createDemoApp(projectId, app);

        gitlabUserId = TypeUtil.objToInteger(userAttrService.baseQueryById(organizationRegisterEventPayload.getUser().getId()).getGitlabUserId());

        if (applicationService.baseQuery(applicationRepDTO.getId()).getGitlabProjectId() == null) {
            throw new CommonException("Creating gitlab project for app {} failed.", applicationRepDTO.getId());
        }

        Integer gitlabProjectId = applicationService.baseQuery(applicationRepDTO.getId()).getGitlabProjectId();

//        2. 创建分支
        BranchDTO branchDO = gitlabServiceClientOperator.queryBranch(gitlabProjectId, demoDataVO.getBranchInfo().getBranchName());
        if (branchDO.getName() == null) {
            gitlabServiceClientOperator.createBranch(
                    gitlabProjectId,
                    demoDataVO.getBranchInfo().getBranchName(),
                    demoDataVO.getBranchInfo().getOriginBranch(),
                    gitlabUserId);
        }

        // 3. 提交代码
        newCommit(gitlabProjectId);

        // 4. 持续集成
        // - 使用pipeline接口查询pipeline状态，状态完成之后进行下一步操作
        //目前选择不等待

        // 5. 创建合并请求并确认
        mergeBranch(gitlabProjectId);

        // 6. 创建标记，由于选择人工造版本数据而不是通过ci，此处tag-name不使用正确的。
        devopsGitService.createTag(projectId, applicationRepDTO.getId(), demoDataVO.getTagInfo().getTag() + "-alpha.1", demoDataVO.getTagInfo().getRef(), demoDataVO.getTagInfo().getMsg(), demoDataVO.getTagInfo().getReleaseNotes());

        createFakeApplicationVersion(applicationRepDTO.getId());

        // 7. 发布应用
//        ApplicationReleasingVO applicationReleasingDTO = demoDataVO.getApplicationRelease();
//        applicationReleasingDTO.setAppServiceId(applicationRepDTO.getId());
//        applicationReleasingDTO.setAppVersions(Collections.singletonList(getApplicationVersion(projectId, applicationRepDTO.getId())));
//        applicationMarketService.create(projectId, applicationReleasingDTO);
    }

    /**
     * 自己重写创建应用方法，在创建应用之后直接调用创建gitlab项目的方法，而不是使用saga.
     * 避免事务之间的值的隔离。
     *
     * @param projectId         项目id
     * @param applicationReqDTO 应用创建的数据
     * @return 应用创建的纪录
     */
    private AppServiceRepVO createDemoApp(Long projectId, AppServiceReqVO applicationReqDTO) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplicationService(applicationReqDTO.getCode());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceDTO applicationDTO = ConvertUtils.convertObject(applicationReqDTO, AppServiceDTO.class);

        Long appId = devopsProjectService.queryAppIdByProjectId(projectId);

        applicationDTO.setAppId(appId);
        applicationService.checkName(appId, applicationDTO.getName());
        applicationService.checkCode(appId, applicationDTO.getCode());

        applicationDTO.setActive(true);
        applicationDTO.setSynchro(false);
        applicationDTO.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());

        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(applicationDTO.getAppId());
        MemberDTO gitlabMember = gitlabGroupMemberService.queryByUserId(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (gitlabMember == null || !Objects.equals(gitlabMember.getAccessLevel(), AccessLevel.OWNER.toValue())) {
            throw new CommonException("error.user.not.owner");
        }
        // 创建saga payload
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setType("application");
        devOpsAppServicePayload.setPath(applicationReqDTO.getCode());
        devOpsAppServicePayload.setOrganizationId(organization.getId());
        devOpsAppServicePayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppServicePayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppServicePayload.setUserIds(applicationReqDTO.getUserIds());
        devOpsAppServicePayload.setSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());

        //设置仓库Id
//        List<DevopsConfigVO> harborConfig = projectConfigService.listByIdAndType(null, "harbor");
//        List<DevopsConfigVO> chartConfig = projectConfigService.listByIdAndType(null, "chart");
//        applicationDTO.setHarborConfigId(harborConfig.get(0).getId());
//        applicationDTO.setChartConfigId(chartConfig.get(0).getId());

        applicationDTO = applicationService.baseCreate(applicationDTO);

        Long appServiceId = applicationDTO.getId();
        if (appServiceId == null) {
            throw new CommonException("error.application.create.insert");
        }

        devOpsAppServicePayload.setAppServiceId(applicationDTO.getId());
        devOpsAppServicePayload.setIamProjectId(projectId);

        // 如果不跳过权限检查
        List<Long> userIds = applicationReqDTO.getUserIds();
        if (!applicationReqDTO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appServiceUserPermissionService.baseCreate(e, appServiceId));
        }

        String input = gson.toJson(devOpsAppServicePayload);

        devopsSagaHandler.createAppService(input);

        return ConvertUtils.convertObject(applicationService.baseQueryByCode(applicationReqDTO.getCode(),
                applicationDTO.getAppId()), AppServiceRepVO.class);
    }



    /**
     * add a new commit to the certain branch
     *
     * @param gitlabProjectId gitlab project id
     */
    private void newCommit(Integer gitlabProjectId) {
        gitlabServiceClientOperator.createFile(gitlabProjectId, "newFile" + UUID.randomUUID().toString().replaceAll("-", ""), "a new commit.", "[ADD] a new file", gitlabUserId, demoDataVO.getBranchInfo().getBranchName());
    }


    /**
     * create a new merge request and confirm.
     *
     * @param gitlabProjectId gitlab project id.
     */
    private void mergeBranch(Integer gitlabProjectId) {
        try {
            // 创建merge request
            MergeRequestDTO mergeRequest = gitlabServiceClientOperator.createMergeRequest(gitlabProjectId, demoDataVO.getBranchInfo().getBranchName(), "master", "a new merge request", "[ADD] add instant push", gitlabUserId);

            // 确认merge request
            gitlabServiceClientOperator.acceptMergeRequest(gitlabProjectId, mergeRequest.getId(), "", Boolean.FALSE, Boolean.TRUE, gitlabUserId);
        } catch (Exception e) {
            logger.error("Error occurred when merge request. Exception is {}", e);
        }
    }


    /**
     * get the application version named "0.1.0"
     *
     * @param projectId     the project id
     * @param applicationId the application id
     * @return the version
//     */
//    private AppMarketVersionVO getApplicationVersion(Long projectId, Long applicationId) {
//        PageRequest pageRequest = new PageRequest(0, 1);
//        PageInfo<ApplicationVersionRespVO> versions = applicationVersionService.pageByOptions(projectId, applicationId, pageRequest, null);
//        if (!versions.getList().isEmpty()) {
//            AppMarketVersionVO appMarketVersionVO = new AppMarketVersionVO();
//            BeanUtils.copyProperties(versions.getList().get(0), appMarketVersionVO);
//            return appMarketVersionVO;
//        } else {
//            logger.error("Error: can not find a version with name {}", demoDataVO.getTagInfo().getTag());
//            return null;
//        }
//    }

    /**
     * 手动制造一个应用版本
     *
     * @param appServiceId application id
     */
    private void createFakeApplicationVersion(Long appServiceId) {
        try {
            byte[] bytes = StreamUtils.copyToByteArray(this.getClass().getClassLoader().getResourceAsStream(tgzFilePath));
            MockMultipartFile multipartFile = new MockMultipartFile("code-i.tgz", "code-i.tgz", "application/tgz", bytes);
            appServiceVersionService.create(demoDataVO.getAppServiceVersionDTO().getImage(), applicationService.baseQuery(appServiceId).getToken(), demoDataVO.getAppServiceVersionDTO().getVersion(), demoDataVO.getAppServiceVersionDTO().getCommit(), multipartFile);
        } catch (IOException e) {
            logger.error("can not find file {}", tgzFilePath);
            throw new CommonException(e);
        }
    }
}
