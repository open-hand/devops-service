package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.DEFAULT_REPO;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_USER_NOT_OWNER;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.MockMultipartFile;
import io.choerodon.devops.app.eventhandler.DevopsSagaHandler;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.app.eventhandler.payload.OrganizationRegisterEventPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import io.choerodon.devops.infra.util.*;

/**
 * 为搭建Demo环境初始化项目中的一些数据，包含应用，分支，提交，版本，应用市场等
 * 目前实现不选择程序中进行循环等待等待ci生成版本，而是手动生成版本，但是该版本可以看
 * 但是会部署失败，显示找不到Chart.
 *
 * @author zmf
 */
@Service
public class DevopsDemoEnvInitServiceImpl implements DevopsDemoEnvInitService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DESTINATION_PATH = "devops";
    private static final String STORE_PATH = "stores";
    private static final String ERROR_VERSION_INSERT = "devops.version.insert";

    @Value("${demo.data.file.path:demo/demo-data.json}")
    private String demoDataFilePath;
    @Value("${demo.tgz.file.path:demo/code-i.tgz}")
    private String tgzFilePath;

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
    private DevopsSagaHandler devopsSagaHandler;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private ChartUtil chartUtil;
    @Autowired
    private AppServiceVersionValueService appServiceVersionValueService;
    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private DevopsHelmConfigService devopsHelmConfigService;

    private final Gson gson = new Gson();

    private DemoDataVO demoDataVO;
    private Integer gitlabUserId;

    @PostConstruct
    public void loadData() {
        try {
            String content = StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream(demoDataFilePath), StandardCharsets.UTF_8);
            demoDataVO = JSONObject.parseObject(content, DemoDataVO.class);
        } catch (Exception e) {
            logger.error("Load content of demo data failed. exception is: {}", e);
        }
    }

    @Override
    public void initialDemoEnv(OrganizationRegisterEventPayload organizationRegisterEventPayload) {
        Long projectId = organizationRegisterEventPayload.getProject().getId();
        // 1. 创建服务
        AppServiceReqVO app = demoDataVO.getApplicationInfo();

        AppServiceRepVO applicationRepDTO = createDemoApp(projectId, app);

        gitlabUserId = TypeUtil.objToInteger(userAttrService.baseQueryById(organizationRegisterEventPayload.getUser().getId()).getGitlabUserId());

        if (applicationService.baseQuery(applicationRepDTO.getId()).getGitlabProjectId() == null) {
            throw new CommonException("Creating gitlab project for app {} failed.", applicationRepDTO.getId());
        }

        Integer gitlabProjectId = applicationService.baseQuery(applicationRepDTO.getId()).getGitlabProjectId();

//        2. 创建分支
        BranchDTO branchDO = gitlabServiceClientOperator.queryBranch(gitlabProjectId, demoDataVO.getBranchInfo().getBranchName());
        if (branchDO.getName() == null) {
            GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator
                    .queryProjectById(gitlabProjectId);
            gitlabServiceClientOperator.createBranch(
                    gitlabProjectId,
                    demoDataVO.getBranchInfo().getBranchName(),
                    gitlabProjectDO.getDefaultBranch(),
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
        Optional<TagVO> tagVO = devopsGitService.pageTagsByOptions(projectId, applicationRepDTO.getId(), null, 0, 100, true).getContent().stream().filter(tagVO1 -> tagVO1.getName().equals(demoDataVO.getTagInfo().getTag() + "-alpha.1")).findFirst();
        if (!tagVO.isPresent()) {
            devopsGitService.createTag(projectId, applicationRepDTO.getId(), demoDataVO.getTagInfo().getTag() + "-alpha.1", demoDataVO.getTagInfo().getRef(), demoDataVO.getTagInfo().getMsg(), demoDataVO.getTagInfo().getReleaseNotes());
        }
        createFakeApplicationVersion(applicationRepDTO.getId());

        // 7.读出应用服务的分支存入devops_branch
        createDevopsBranch(gitlabProjectId, gitlabUserId, applicationRepDTO, organizationRegisterEventPayload.getUser().getId());
    }

    private void createDevopsBranch(Integer gitlabProjectId, Integer gitlabUserId, AppServiceRepVO appServiceRepVO, Long userId) {
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listBranch(gitlabProjectId, gitlabUserId);
        if (!CollectionUtils.isEmpty(branchDTOS)) {
            branchDTOS.forEach(branchDTO -> {
                CommitDTO commitDTO = branchDTO.getCommit();
                Date checkoutDate = commitDTO.getCommittedDate();
                String checkoutSha = commitDTO.getId();
                DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
                devopsBranchDTO.setAppServiceId(appServiceRepVO.getId());
                devopsBranchDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsBranchDTO.setCheckoutDate(checkoutDate);
                devopsBranchDTO.setCheckoutCommit(checkoutSha);
                devopsBranchDTO.setBranchName(branchDTO.getName());
                devopsBranchDTO.setUserId(userId);

                devopsBranchDTO.setLastCommitDate(checkoutDate);
                devopsBranchDTO.setLastCommit(checkoutSha);
                devopsBranchDTO.setLastCommitMsg(LogUtil.cutOutString(commitDTO.getMessage(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));
                devopsBranchDTO.setLastCommitUser(userId);
                devopsBranchService.baseCreate(devopsBranchDTO);
            });
        }
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
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceDTO applicationDTO = ConvertUtils.convertObject(applicationReqDTO, AppServiceDTO.class);

        applicationDTO.setProjectId(projectId);
        applicationService.checkName(projectId, applicationDTO.getName());
        applicationService.checkCode(projectId, applicationDTO.getCode());

        applicationDTO.setActive(true);
        applicationDTO.setSynchro(false);
        applicationDTO.setToken(GenerateUUID.generateUUID());

        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(applicationDTO.getProjectId());

        boolean isGitlabRoot = false;

        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        if (!isGitlabRoot) {
            MemberDTO gitlabMember = gitlabGroupMemberService.queryByUserId(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (gitlabMember == null || !Objects.equals(gitlabMember.getAccessLevel(), AccessLevel.OWNER.toValue())) {
                throw new CommonException(DEVOPS_USER_NOT_OWNER);
            }
        }

        // 创建saga payload
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setType("application");
        devOpsAppServicePayload.setPath(applicationReqDTO.getCode());
        devOpsAppServicePayload.setOrganizationId(organization.getTenantId());
        devOpsAppServicePayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppServicePayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppServicePayload.setUserIds(Collections.emptyList());

        applicationDTO = applicationService.baseCreate(applicationDTO);

        Long appServiceId = applicationDTO.getId();
        if (appServiceId == null) {
            throw new CommonException("devops.application.create.insert");
        }

        devOpsAppServicePayload.setAppServiceId(applicationDTO.getId());
        devOpsAppServicePayload.setIamProjectId(projectId);

        String input = gson.toJson(devOpsAppServicePayload);

        devopsSagaHandler.createAppService(input);

        return ConvertUtils.convertObject(applicationService.baseQueryByCode(applicationReqDTO.getCode(),
                applicationDTO.getProjectId()), AppServiceRepVO.class);
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
            MergeRequestDTO mergeRequest = gitlabServiceClientOperator.createMergeRequest(gitlabProjectId, demoDataVO.getBranchInfo().getBranchName(), demoDataVO.getBranchInfo().getOriginBranch(), "a new merge request", "[ADD] add instant push", gitlabUserId);

            // 确认merge request
            gitlabServiceClientOperator.acceptMergeRequest(gitlabProjectId, mergeRequest.getId(), "", Boolean.FALSE, Boolean.TRUE, gitlabUserId);
        } catch (Exception e) {
            logger.error("Error occurred when merge request. Exception is {}", e);
        }
    }

    /**
     * 手动制造一个应用版本
     *
     * @param appServiceId application id
     */
    private void createFakeApplicationVersion(Long appServiceId) {
        try {
            byte[] bytes = StreamUtils.copyToByteArray(this.getClass().getClassLoader().getResourceAsStream(tgzFilePath));
            MockMultipartFile multipartFile = new MockMultipartFile("code-i.tgz", "code-i.tgz", "application/tgz", bytes);
            //harborConfigId需要传入
            doCreate(demoDataVO.getAppServiceVersionDTO().getImage(), applicationService.baseQuery(appServiceId).getToken(), demoDataVO.getAppServiceVersionDTO().getVersion(), demoDataVO.getAppServiceVersionDTO().getCommit(), multipartFile);
        } catch (IOException e) {
            logger.error("can not find file {}", tgzFilePath);
            throw new CommonException(e);
        }
    }

    private void doCreate(String image, String token, String version, String commit, MultipartFile files) {
        AppServiceDTO appServiceDTO = appServiceMapper.queryByToken(token);

        AppServiceVersionValueDTO appServiceVersionValueDTO = new AppServiceVersionValueDTO();
        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceVersionDTO newApplicationVersion = appServiceVersionService.baseQueryByAppServiceIdAndVersion(appServiceDTO.getId(), version);
        appServiceVersionDTO.setAppServiceId(appServiceDTO.getId());
        appServiceVersionDTO.setImage(image);
        appServiceVersionDTO.setCommit(commit);
        appServiceVersionDTO.setVersion(version);
        appServiceVersionDTO.setHarborConfigId(1L);
        appServiceVersionDTO.setRepoType(DEFAULT_REPO);
        appServiceVersionDTO.setRef(GitOpsConstants.MASTER);

        // 查询helm仓库配置id
        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigService.queryAppConfig(appServiceDTO.getId(), projectDTO.getId(), organization.getTenantId());

        ConfigVO helmConfig = ConvertUtils.convertObject(devopsHelmConfigDTO, ConfigVO.class);
        helmConfig.setIsPrivate(devopsHelmConfigDTO.getRepoPrivate());
        String helmUrl = helmConfig.getUrl();
        appServiceVersionDTO.setHelmConfigId(devopsHelmConfigDTO.getId());

        appServiceVersionDTO.setRepository(helmUrl.endsWith("/") ? helmUrl + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/" : helmUrl + "/" + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/");
        String storeFilePath = STORE_PATH + version;

        String destFilePath = DESTINATION_PATH + version;
        String path = FileUtil.multipartFileToFile(storeFilePath, files);
        //上传chart包到chartmuseum
        chartUtil.uploadChart(helmUrl, organization.getTenantNum(), projectDTO.getDevopsComponentCode(), new File(path), helmConfig.getUsername(), helmConfig.getPassword());

        // 有需求让重新上传chart包，所以校验重复推后
        if (newApplicationVersion != null) {
            FileUtil.deleteDirectories(storeFilePath);
            return;
        }
        FileUtil.unTarGZ(path, destFilePath);

        // 使用深度优先遍历查找文件, 避免查询到子chart的values值
        File valuesFile = FileUtil.queryFileFromFilesBFS(new File(destFilePath), "values.yaml");

        if (valuesFile == null) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException("devops.find.values.yaml.in.chart");
        }

        String values;
        try (FileInputStream fis = new FileInputStream(valuesFile)) {
            values = FileUtil.replaceReturnString(fis, null);
        } catch (IOException e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException(e);
        }

        try {
            FileUtil.checkYamlFormat(values);
        } catch (CommonException e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException("The format of the values.yaml in the chart is invalid!", e);
        }
        appServiceVersionValueDTO.setValue(values);
        try {
            appServiceVersionDTO.setValueId(appServiceVersionValueService
                    .baseCreate(appServiceVersionValueDTO).getId());
        } catch (Exception e) {
            FileUtil.deleteDirectories(storeFilePath, destFilePath);
            throw new CommonException(ERROR_VERSION_INSERT, e);
        }

        AppServiceVersionReadmeDTO appServiceVersionReadmeDTO = new AppServiceVersionReadmeDTO();
        appServiceVersionReadmeDTO.setReadme(FileUtil.getReadme(destFilePath));
        appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO);

        appServiceVersionDTO.setReadmeValueId(appServiceVersionReadmeDTO.getId());
        appServiceVersionService.baseCreate(appServiceVersionDTO);

        FileUtil.deleteDirectories(destFilePath, storeFilePath);
        sendNotificationService.sendWhenAppServiceVersion(appServiceVersionDTO, appServiceDTO, projectDTO);
    }
}
