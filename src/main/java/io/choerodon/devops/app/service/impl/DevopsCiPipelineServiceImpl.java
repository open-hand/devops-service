package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.GitOpsConstants.DEFAULT_PIPELINE_RECORD_SIZE;
import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import sun.misc.BASE64Decoder;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/2 18:00
 */
@Service
public class DevopsCiPipelineServiceImpl implements DevopsCiPipelineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCiPipelineServiceImpl.class);

    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";
    private static final String DISABLE_PIPELINE_FAILED = "disable.pipeline.failed";
    private static final String ENABLE_PIPELINE_FAILED = "enable.pipeline.failed";
    private static final String DELETE_PIPELINE_FAILED = "delete.pipeline.failed";
    private static final String ERROR_USER_HAVE_NO_APP_PERMISSION = "error.user.have.no.app.permission";
    private static final String ERROR_APP_SVC_ID_IS_NULL = "error.app.svc.id.is.null";
    private static final String ERROR_PROJECT_ID_IS_NULL = "error.project.id.is.null";
    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";
    private static final String ERROR_UNSUPPORTED_STEP_TYPE = "error.unsupported.step.type";
    private static final String ERROR_BRANCH_PERMISSION_MISMATCH = "error.branch.permission.mismatch";

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    private final DevopsCiPipelineMapper devopsCiPipelineMapper;
    private final DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    private final DevopsCiStageService devopsCiStageService;
    private final DevopsCiJobService devopsCiJobService;
    private final DevopsCiContentService devopsCiContentService;
    private final GitlabServiceClientOperator gitlabServiceClientOperator;
    private final UserAttrService userAttrService;
    private final AppServiceService appServiceService;
    private final DevopsCiJobRecordService devopsCiJobRecordService;
    private final DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    private final DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private final DevopsProjectService devopsProjectService;
    private final BaseServiceClientOperator baseServiceClientOperator;
    private final RdupmClientOperator rdupmClientOperator;
    private final CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    private final DevopsConfigService devopsConfigService;
    private final PermissionHelper permissionHelper;
    private final AppServiceMapper appServiceMapper;
    private final CiCdPipelineMapper ciCdPipelineMapper;
    private final DevopsCdStageService devopsCdStageService;
    private final DevopsCdAuditService devopsCdAuditService;
    private final PipelineAppDeployService pipelineAppDeployService;
    private final DevopsCdJobService devopsCdJobService;
    private final DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    private final DevopsCdJobRecordService devopsCDJobRecordService;
    private final DevopsCdStageRecordService devopsCdStageRecordService;


    public DevopsCiPipelineServiceImpl(
            @Lazy DevopsCiPipelineMapper devopsCiPipelineMapper,
            // 这里的懒加载是为了避免循环依赖
            @Lazy DevopsCiPipelineRecordService devopsCiPipelineRecordService,
            DevopsCiStageService devopsCiStageService,
            @Lazy DevopsCiJobService devopsCiJobService,
            DevopsCiContentService devopsCiContentService,
            @Lazy GitlabServiceClientOperator gitlabServiceClientOperator,
            UserAttrService userAttrService,
            CheckGitlabAccessLevelService checkGitlabAccessLevelService,
            @Lazy
                    AppServiceService appServiceService,
            DevopsCiJobRecordService devopsCiJobRecordService,
            DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper,
            DevopsProjectService devopsProjectService,
            BaseServiceClientOperator baseServiceClientOperator,
            RdupmClientOperator rdupmClientOperator,
            DevopsConfigService devopsConfigService,
            PermissionHelper permissionHelper,
            AppServiceMapper appServiceMapper,
            DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper, CiCdPipelineMapper ciCdPipelineMapper, DevopsCdStageService devopsCdStageService, DevopsCdAuditService devopsCdAuditService, PipelineAppDeployService pipelineAppDeployService, DevopsCdJobService devopsCdJobService, DevopsCdPipelineRecordService devopsCdPipelineRecordService, DevopsCdJobRecordService devopsCDJobRecordService, DevopsCdStageRecordService devopsCdStageRecordService) {
        this.devopsCiPipelineMapper = devopsCiPipelineMapper;
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
        this.devopsCiContentService = devopsCiContentService;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.appServiceService = appServiceService;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiMavenSettingsMapper = devopsCiMavenSettingsMapper;
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.baseServiceClientOperator = baseServiceClientOperator;
        this.devopsProjectService = devopsProjectService;
        this.rdupmClientOperator = rdupmClientOperator;
        this.devopsConfigService = devopsConfigService;
        this.checkGitlabAccessLevelService = checkGitlabAccessLevelService;
        this.permissionHelper = permissionHelper;
        this.appServiceMapper = appServiceMapper;
        this.ciCdPipelineMapper = ciCdPipelineMapper;
        this.devopsCdStageService = devopsCdStageService;
        this.devopsCdAuditService = devopsCdAuditService;
        this.pipelineAppDeployService = pipelineAppDeployService;
        this.devopsCdJobService = devopsCdJobService;
        this.devopsCdPipelineRecordService = devopsCdPipelineRecordService;
        this.devopsCDJobRecordService = devopsCDJobRecordService;
        this.devopsCdStageRecordService = devopsCdStageRecordService;
    }

    private static String buildSettings(List<MavenRepoVO> mavenRepoList) {
        List<Server> servers = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();

        mavenRepoList.forEach(m -> {
            String[] types = Objects.requireNonNull(m.getType()).split(GitOpsConstants.COMMA);
            if (types.length > 2) {
                throw new CommonException(ERROR_CI_MAVEN_REPOSITORY_TYPE, m.getType());
            }
            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
            }
            repositories.add(new Repository(
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getUrl()),
                    new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
                    new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
        });
        return MavenSettingsUtil.generateMavenSettings(servers, repositories);
    }

    /**
     * 第一次创建CI流水线时初始化仓库下的.gitlab-ci.yml文件
     *
     * @param gitlabProjectId  gitlab项目id
     * @param ciFileIncludeUrl include中的链接
     */
    private void initGitlabCiFile(Integer gitlabProjectId, String ciFileIncludeUrl) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, GitOpsConstants.MASTER, GitOpsConstants.GITLAB_CI_FILE_NAME);

        if (repositoryFile == null) {
            // 说明项目下还没有CI文件
            // 创建文件
            try {
                LOGGER.info("initGitlabCiFile: create .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.createFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        buildIncludeYaml(ciFileIncludeUrl),
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId(),
                        GitOpsConstants.MASTER);
            } catch (Exception ex) {
                throw new CommonException("error.create.or.update.gitlab.ci", ex);
            }

        } else {
            // 将原先的配置文件内容注释并放在原本文件中
            String originFileContent = new String(Base64.getDecoder().decode(repositoryFile.getContent().getBytes()), StandardCharsets.UTF_8);
            // 注释后的内容
            String commentedLines = GitlabCiUtil.commentLines(originFileContent);
            try {
                // 更新文件
                LOGGER.info("initGitlabCiFile: update .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.updateFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        buildIncludeYaml(ciFileIncludeUrl) + GitOpsConstants.NEW_LINE + commentedLines,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId());
            } catch (Exception ex) {
                throw new CommonException("error.create.or.update.gitlab.ci", ex);
            }
        }
    }

    @Override
    @Transactional
    public CiCdPipelineDTO create(Long projectId, CiCdPipelineVO ciCdPipelineVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CI_PIPELINE_CREATE);
        Long iamUserId = TypeUtil.objToLong(GitUserNameUtil.getUserId());
        checkUserPermission(ciCdPipelineVO.getAppServiceId(), iamUserId);
        ciCdPipelineVO.setProjectId(projectId);

        // 设置默认镜像
        if (StringUtils.isEmpty(ciCdPipelineVO.getImage())) {
            ciCdPipelineVO.setImage(defaultCiImage);
        }
        // 创建CICD流水线
        CiCdPipelineDTO ciCdPipelineDTO = ConvertUtils.convertObject(ciCdPipelineVO, CiCdPipelineDTO.class);
        ciCdPipelineDTO.setToken(GenerateUUID.generateUUID());
        if (ciCdPipelineMapper.insertSelective(ciCdPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }

        // 1.保存ci stage信息
        saveCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        // 2.保存cd stage信息
        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineDTO.getId());
    }

    private void saveCdPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        //2.保存cd stage 的信息
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCdStageVOS())) {
            ciCdPipelineVO.getDevopsCdStageVOS().forEach(devopsCdStageVO -> {
                DevopsCdStageDTO devopsCdStageDTO = ConvertUtils.convertObject(devopsCdStageVO, DevopsCdStageDTO.class);
                devopsCdStageDTO.setPipelineId(ciCdPipelineDTO.getId());
                devopsCdStageDTO.setProjectId(projectId);
                DevopsCdStageDTO cdStageDTO = devopsCdStageService.create(devopsCdStageDTO);
                //保存审核人员信息,手动流转才有审核人员信息
                createUserRel(devopsCdStageVO.getCdAuditUserIds(), null, cdStageDTO.getId(), null);
                // 保存cd job信息
                if (!CollectionUtils.isEmpty(devopsCdStageVO.getJobList())) {
                    devopsCdStageVO.getJobList().forEach(devopsCdJobVO -> {
                        // 添加人工卡点的任务类型时才 保存审核人员信息
                        createCdJob(devopsCdJobVO, projectId, devopsCdStageDTO.getId(), ciCdPipelineDTO.getId());
                    });
                }
            });
        }
    }

    private void saveCiPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                devopsCiStageDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存ci job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobVO.setId(devopsCiJobService.create(devopsCiJobDTO).getId());
                    });
                }
            });
            // 保存ci配置文件
            saveCiContent(projectId, ciCdPipelineDTO.getId(), ciCdPipelineVO);

            AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
            String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, ciCdPipelineDTO.getToken());
            initGitlabCiFile(appServiceDTO.getGitlabProjectId(), ciFileIncludeUrl);
        }
    }

    @Override
    public CiCdPipelineVO query(Long projectId, Long pipelineId) {
        // 根据pipeline_id查询数据
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        CommonExAssertUtil.assertTrue(ciCdPipelineDTO != null, "error.pipeline.not.exist", pipelineId);
        CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(ciCdPipelineDTO, CiCdPipelineVO.class);

        //查询CI相关的阶段以及JOB
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(pipelineId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(pipelineId);

        List<DevopsCiStageVO> devopsCiStageVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageVO.class);
        List<DevopsCiJobVO> devopsCiJobVOS = ConvertUtils.convertList(devopsCiJobDTOS, DevopsCiJobVO.class);

        // 封装CI对象
        Map<Long, List<DevopsCiJobVO>> ciJobMap = devopsCiJobVOS.stream().collect(Collectors.groupingBy(DevopsCiJobVO::getCiStageId));
        devopsCiStageVOS.forEach(devopsCiStageVO -> {
            List<DevopsCiJobVO> ciJobVOS = ciJobMap.getOrDefault(devopsCiStageVO.getId(), Collections.emptyList());
            ciJobVOS.sort(Comparator.comparingLong(DevopsCiJobVO::getId));
            devopsCiStageVO.setJobList(ciJobVOS);
        });
        // ci stage排序
        devopsCiStageVOS = devopsCiStageVOS.stream().sorted(Comparator.comparing(DevopsCiStageVO::getSequence)).collect(Collectors.toList());
        for (DevopsCiStageVO devopsCiStageVO : devopsCiStageVOS) {
            devopsCiStageVO.setType(StageType.CI.getType());
        }
        ciCdPipelineVO.setDevopsCiStageVOS(devopsCiStageVOS);

        //查询CD相关的阶段以及JOB
        List<DevopsCdStageDTO> devopsCdStageDTOS = devopsCdStageService.queryByPipelineId(pipelineId);
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobService.listByPipelineId(pipelineId);

        List<DevopsCdStageVO> devopsCdStageVOS = ConvertUtils.convertList(devopsCdStageDTOS, DevopsCdStageVO.class);
        List<DevopsCdJobVO> devopsCdJobVOS = ConvertUtils.convertList(devopsCdJobDTOS, DevopsCdJobVO.class);
        // 封装CD对象
        Map<Long, List<DevopsCdJobVO>> cdJobMap = devopsCdJobVOS.stream().collect(Collectors.groupingBy(DevopsCdJobVO::getStageId));
        devopsCdStageVOS.stream().forEach(devopsCdStageVO -> {
            List<DevopsCdJobVO> jobMapOrDefault = cdJobMap.getOrDefault(devopsCdStageVO.getId(), Collections.emptyList());
            jobMapOrDefault.sort(Comparator.comparing(DevopsCdJobVO::getId));
            devopsCdStageVO.setJobList(jobMapOrDefault);
        });
        // cd stage排序
        devopsCdStageVOS = devopsCdStageVOS.stream().sorted(Comparator.comparing(DevopsCdStageVO::getSequence)).collect(Collectors.toList());
        ciCdPipelineVO.setDevopsCdStageVOS(devopsCdStageVOS);
        return ciCdPipelineVO;
    }

    @Override
    public DevopsCiPipelineDTO queryByAppSvcId(Long id) {
        if (id == null) {
            throw new CommonException(ERROR_APP_SVC_ID_IS_NULL);
        }
        DevopsCiPipelineDTO devopsCiPipelineDTO = new DevopsCiPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(id);
        return devopsCiPipelineMapper.selectOne(devopsCiPipelineDTO);
    }

    @Override
    public List<CiCdPipelineVO> listByProjectIdAndAppName(Long projectId, String name) {
        if (projectId == null) {
            throw new CommonException(ERROR_PROJECT_ID_IS_NULL);
        }
        // 应用有权限的应用服务
        Long userId = DetailsHelper.getUserDetails().getUserId();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        Set<Long> appServiceIds;
        if (projectOwner) {
        appServiceIds = appServiceMapper.listByActive(projectId).stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
        } else {
            appServiceIds = appServiceService.getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new ArrayList<>();
            }
        }

        // 查询流水线
        List<CiCdPipelineVO> ciCdPipelineVOS = ciCdPipelineMapper.queryByProjectIdAndName(projectId, appServiceIds, name);
        // 封装流水线记录
        PageRequest ciPageable = new PageRequest(GitOpsConstants.FIRST_PAGE_INDEX, DEFAULT_PIPELINE_RECORD_SIZE, new Sort(new Sort.Order(Sort.Direction.DESC, DevopsCiPipelineRecordDTO.FIELD_GITLAB_PIPELINE_ID)));
        PageRequest cdPpageable = new PageRequest(GitOpsConstants.FIRST_PAGE_INDEX, DEFAULT_PIPELINE_RECORD_SIZE, new Sort(new Sort.Order(Sort.Direction.DESC, DevopsCdPipelineRecordDTO.FIELD_ID)));

        ciCdPipelineVOS.forEach(ciCdPipelineVO -> {
            List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS = new ArrayList<>();
            Map<Long, List<DevopsCdPipelineRecordVO>> cdPipielineMap = new HashMap<>();
            Map<Long, List<DevopsCiPipelineRecordVO>> ciPipielineMap = new HashMap<>();

            //查询ci流水线记录
            Page<DevopsCiPipelineRecordVO> pipelineCiRecordVOPageInfo = devopsCiPipelineRecordService.pagingPipelineRecord(projectId, ciCdPipelineVO.getId(), ciPageable);
            if (!CollectionUtils.isEmpty(pipelineCiRecordVOPageInfo.getContent())) {
                ciCdPipelineVO.setLatestExecuteDate(pipelineCiRecordVOPageInfo.getContent().get(0).getCreatedDate());
                ciCdPipelineVO.setLatestExecuteStatus(pipelineCiRecordVOPageInfo.getContent().get(0).getStatus());
                ciCdPipelineVO.setHasMoreRecords(pipelineCiRecordVOPageInfo.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
                ciPipielineMap = pipelineCiRecordVOPageInfo.getContent().stream().collect(Collectors.groupingBy(DevopsCiPipelineRecordVO::getGitlabPipelineId));
            }

            //查询cd流水线记录
            Page<DevopsCdPipelineRecordVO> devopsCdPipelineRecordVOS = devopsCdPipelineRecordService.pagingCdPipelineRecord(projectId, ciCdPipelineVO.getId(), cdPpageable);
            if (!CollectionUtils.isEmpty(devopsCdPipelineRecordVOS.getContent())) {
                ciCdPipelineVO.setLatestExecuteDate(devopsCdPipelineRecordVOS.getContent().get(0).getCreatedDate());
                ciCdPipelineVO.setLatestExecuteStatus(devopsCdPipelineRecordVOS.getContent().get(0).getStatus());
                ciCdPipelineVO.setHasMoreRecords(pipelineCiRecordVOPageInfo.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
                cdPipielineMap = devopsCdPipelineRecordVOS.getContent().stream().collect(Collectors.groupingBy(DevopsCdPipelineRecordVO::getGitlabPipelineId));
            }

            //封装 CiCdPipelineRecordVO
            //纯ci流水线
            if (!CollectionUtils.isEmpty(ciPipielineMap) && CollectionUtils.isEmpty(cdPipielineMap)) {
                for (Map.Entry<Long, List<DevopsCiPipelineRecordVO>> longListEntry : ciPipielineMap.entrySet()) {
                    List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                    CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                    //收集这条ci流水线的所有stage
                    DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = longListEntry.getValue().get(0);
                    ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
                    ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
                    ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
                    ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());

                    stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
                    ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
                }
            }
            if (!CollectionUtils.isEmpty(cdPipielineMap) && CollectionUtils.isEmpty(ciPipielineMap)) {
                for (Map.Entry<Long, List<DevopsCdPipelineRecordVO>> longListEntry : cdPipielineMap.entrySet()) {
                    List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                    CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                    //收集这条ci流水线的所有stage
                    DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = longListEntry.getValue().get(0);
                    ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
                    ciCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordVO.getCreatedDate());
                    ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
                    ciCdPipelineRecordVO.setGitlabPipelineId(Objects.isNull(devopsCdPipelineRecordVO.getGitlabPipelineId()) ? null : devopsCdPipelineRecordVO.getGitlabPipelineId());

                    stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
                    ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
                }

            }
            if (!CollectionUtils.isEmpty(cdPipielineMap) && !CollectionUtils.isEmpty(ciPipielineMap)) {
                for (Map.Entry<Long, List<DevopsCiPipelineRecordVO>> longListEntry : ciPipielineMap.entrySet()) {
                    List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                    CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                    //收集这条ci流水线的所有stage
                    DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = longListEntry.getValue().get(0);
                    ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
                    ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
                    ciCdPipelineRecordVO.setCdRecordId(devopsCiPipelineRecordVO.getId());
                    ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());

                    stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());

                    List<DevopsCdPipelineRecordVO> cdPipelineRecordVOS = cdPipielineMap.get(longListEntry.getKey());
                    if (!CollectionUtils.isEmpty(cdPipelineRecordVOS)) {
                        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = cdPipelineRecordVOS.get(0);
                        ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
                        ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
                        stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
                    }
                    ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                    ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
                }
            }
            ciCdPipelineVO.setCiCdPipelineRecordVOS(ciCdPipelineRecordVOS);
        });
        return ciCdPipelineVOS;
    }

    @Override
    public DevopsCiPipelineVO queryById(Long ciPipelineId) {
        return devopsCiPipelineMapper.queryById(ciPipelineId);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO disablePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_STATUS_UPDATE);
        if (ciCdPipelineMapper.disablePipeline(pipelineId) != 1) {
            throw new CommonException(DISABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    @Override
    @Transactional
    public void deletePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_DELETE);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
        // 校验用户是否有应用服务权限
        Long userId = DetailsHelper.getUserDetails().getUserId();
        checkUserPermission(appServiceDTO.getId(), userId);
        // 删除流水线
        if (ciCdPipelineMapper.deleteByPrimaryKey(pipelineId) != 1) {
            throw new CommonException(DELETE_PIPELINE_FAILED);
        }
        // 删除stage
        devopsCiStageService.deleteByPipelineId(pipelineId);
        devopsCdStageService.deleteByPipelineId(pipelineId);

        // 删除job
        devopsCiJobService.deleteByPipelineId(pipelineId);
        devopsCdJobService.deleteByPipelineId(pipelineId);

        // 删除 ci job记录
        devopsCiJobRecordService.deleteByGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());

        // 删除pipeline记录
        devopsCiPipelineRecordService.deleteByGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());
        //删除 cd  pipeline记录 stage记录 以及Job记录
        devopsCdPipelineRecordService.deleteByPipelineId(pipelineId);

        // 删除content file
        devopsCiContentService.deleteByPipelineId(pipelineId);

        // 删除.gitlab-ci.yaml文件
        deleteGitlabCiFile(appServiceDTO.getGitlabProjectId());
    }

    @Override
    @Transactional
    public CiCdPipelineDTO enablePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_STATUS_UPDATE);
        if (ciCdPipelineMapper.enablePipeline(pipelineId) != 1) {
            throw new CommonException(ENABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    @Override
    public void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_NEW_PERFORM);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, userAttrDTO.getGitlabUserId(), gitlabProjectId, ref);
        //触发ci流水线
        Pipeline pipeline = gitlabServiceClientOperator.createPipeline(gitlabProjectId.intValue(), userAttrDTO.getGitlabUserId().intValue(), ref);
        // 保存执行记录
        try {
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.create(pipelineId, gitlabProjectId, pipeline);
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(), pipeline.getId(), userAttrDTO.getGitlabUserId().intValue());
            devopsCiJobRecordService.create(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTOS, userAttrDTO.getIamUserId());
        } catch (Exception e) {
            LOGGER.info("save pipeline Records failed， ciPipelineId {}.", pipelineId);
        }
    }


    public void checkUserBranchPushPermission(Long projectId, Long gitlabUserId, Long gitlabProjectId, String ref) {
        BranchDTO branchDTO = gitlabServiceClientOperator.getBranch(gitlabProjectId.intValue(), ref);
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(devopsProjectDTO.getDevopsAppGroupId().intValue(), gitlabUserId.intValue());
        if (memberDTO == null || memberDTO.getId() == null) {
            memberDTO = gitlabServiceClientOperator.getMember(gitlabProjectId, gitlabUserId);
        }
        if (Boolean.TRUE.equals(branchDTO.getProtected())
                && Boolean.FALSE.equals(branchDTO.getDevelopersCanMerge())
                && Boolean.FALSE.equals(branchDTO.getDevelopersCanPush())
                && memberDTO.getAccessLevel() <= AccessLevel.DEVELOPER.toValue()) {
            throw new CommonException(ERROR_BRANCH_PERMISSION_MISMATCH, ref);
        }
    }

    @Override
    public int selectCountByAppServiceId(Long appServiceId) {
        DevopsCiPipelineDTO devopsCiPipelineDTO = new DevopsCiPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(Objects.requireNonNull(appServiceId));
        return devopsCiPipelineMapper.selectCount(devopsCiPipelineDTO);
    }

    private void deleteGitlabCiFile(Integer gitlabProjectId) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, GitOpsConstants.MASTER, GitOpsConstants.GITLAB_CI_FILE_NAME);
        if (repositoryFile != null) {
            try {
                LOGGER.info("deleteGitlabCiFile: delete .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.deleteFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId());
            } catch (Exception e) {
                throw new CommonException("error.delete.gitlab-ci.file", e);
            }

        }
    }

    /**
     * 校验用户是否拥有应用服务权限
     *
     * @param appServiceId 应用服务id
     * @param iamUserId    用户id
     */
    private void checkUserPermission(Long appServiceId, Long iamUserId) {
        if (!appServiceService.checkAppServicePermissionForUser(appServiceId, iamUserId)) {
            throw new CommonException(ERROR_USER_HAVE_NO_APP_PERMISSION);
        }

    }

    private static String buildIncludeYaml(String ciFileIncludeUrl) {
        GitlabCi gitlabCi = new GitlabCi();
        gitlabCi.setInclude(ciFileIncludeUrl);
        return GitlabCiUtil.gitlabCi2yaml(gitlabCi);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO update(Long projectId, Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_UPDATE);
        Long userId = DetailsHelper.getUserDetails().getUserId();
        checkUserPermission(ciCdPipelineVO.getAppServiceId(), userId);
        // 校验自定义任务格式
        CiCdPipelineDTO ciCdPipelineDTO = ConvertUtils.convertObject(ciCdPipelineVO, CiCdPipelineDTO.class);
        ciCdPipelineDTO.setId(pipelineId);
        if (ciCdPipelineMapper.updateByPrimaryKeySelective(ciCdPipelineDTO) != 1) {
            throw new CommonException(UPDATE_PIPELINE_FAILED);
        }
        //更新CI流水线
        updateCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        //更新CD流水线
        updateCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    private void updateCdPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        //cd stage 先删除，后新增
        List<DevopsCdStageDTO> cdStageDTOS = devopsCdStageService.queryByPipelineId(ciCdPipelineDTO.getId());
        cdStageDTOS.forEach(devopsCdStageDTO -> {
            //删除cd的阶段以及关联的审核人员数据
            devopsCdStageService.deleteById(devopsCdStageDTO.getId());
            devopsCdJobService.deleteByStageId(devopsCdStageDTO.getId());
        });
        //新增
        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
    }

    private void updateCiPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        // 更新stage
        // 查询数据库中原有stage列表,并和新的stage列表作比较。
        // 差集：要删除的记录
        // 交集：要更新的记录
        List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(ciCdPipelineDTO.getId());
        Set<Long> oldStageIds = devopsCiStageDTOS.stream().map(DevopsCiStageDTO::getId).collect(Collectors.toSet());

        Set<Long> updateIds = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                .filter(devopsCiStageVO -> devopsCiStageVO.getId() != null)
                .map(DevopsCiStageVO::getId)
                .collect(Collectors.toSet());
        // 去掉要更新的记录，剩下的为要删除的记录
        oldStageIds.removeAll(updateIds);
        oldStageIds.forEach(stageId -> {
            devopsCiStageService.deleteById(stageId);
            devopsCiJobService.deleteByStageId(stageId);
        });

        ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
            if (devopsCiStageVO.getId() != null) {
                // 更新
                devopsCiStageService.update(devopsCiStageVO);
                devopsCiJobService.deleteByStageId(devopsCiStageVO.getId());
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setId(null);
                        devopsCiJobDTO.setCiStageId(devopsCiStageVO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);
                        devopsCiJobVO.setId(devopsCiJobDTO.getId());
                    });
                }
            } else {
                // 新增
                devopsCiStageVO.setCiPipelineId(ciCdPipelineDTO.getId());
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);
                        devopsCiJobVO.setId(devopsCiJobDTO.getId());
                    });
                }
            }
        });
        saveCiContent(projectId, ciCdPipelineDTO.getId(), ciCdPipelineVO);
    }


    private void saveCiContent(final Long projectId, Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        GitlabCi gitlabCi = buildGitLabCiObject(projectId, ciCdPipelineVO);
        StringBuilder gitlabCiYaml = new StringBuilder(GitlabCiUtil.gitlabCi2yaml(gitlabCi));

        // 拼接自定义job
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            List<DevopsCiJobVO> ciJobVOS = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                    .flatMap(v -> v.getJobList().stream()).filter(job -> JobTypeEnum.CUSTOM.value().equalsIgnoreCase(job.getType()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ciJobVOS)) {
                for (DevopsCiJobVO job : ciJobVOS) {
                    gitlabCiYaml.append(GitOpsConstants.NEW_LINE).append(job.getMetadata());
                }
            }

        }

        //保存gitlab-ci配置文件
        DevopsCiContentDTO devopsCiContentDTO = new DevopsCiContentDTO();
        devopsCiContentDTO.setCiPipelineId(pipelineId);
        devopsCiContentDTO.setCiContentFile(gitlabCiYaml.toString());
        devopsCiContentService.create(devopsCiContentDTO);
    }

    /**
     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
     *
     * @param projectId      项目id
     * @param ciCdPipelineVO 流水线数据
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(final Long projectId, CiCdPipelineVO ciCdPipelineVO) {
        // 对阶段排序
        List<String> stages = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                .sorted(Comparator.comparing(DevopsCiStageVO::getSequence))
                .map(DevopsCiStageVO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();

        // 如果用户指定了就使用用户指定的，如果没有指定就使用默认的猪齿鱼提供的镜像
        gitlabCi.setImage(StringUtils.isEmpty(ciCdPipelineVO.getImage()) ? defaultCiImage : ciCdPipelineVO.getImage());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        gitlabCi.setStages(stages);
        ciCdPipelineVO.getDevopsCiStageVOS().forEach(stageVO -> {
            if (!CollectionUtils.isEmpty(stageVO.getJobList())) {
                stageVO.getJobList().forEach(job -> {
                    if (JobTypeEnum.CUSTOM.value().equals(job.getType())) {
                        return;
                    }
                    CiJob ciJob = new CiJob();
                    if (!StringUtils.isEmpty(job.getImage())) {
                        ciJob.setImage(job.getImage());
                    }
                    ciJob.setStage(stageVO.getName());
                    ciJob.setScript(buildScript(Objects.requireNonNull(projectDTO.getOrganizationId()), projectId, job));
                    ciJob.setCache(buildJobCache(job));
                    processOnlyAndExcept(job, ciJob);
                    gitlabCi.addJob(job.getName(), ciJob);
                });
            }
        });
        buildBeforeScript(gitlabCi);
        return gitlabCi;
    }

    /**
     * 处理job的触发方式
     *
     * @param metadata job元数据
     * @param ciJob    ci文件的job对象
     */
    private void processOnlyAndExcept(DevopsCiJobVO metadata, CiJob ciJob) {
        if (StringUtils.isNotBlank(metadata.getTriggerType())
                && StringUtils.isNotBlank(metadata.getTriggerValue())) {
            CiTriggerType ciTriggerType = CiTriggerType.forValue(metadata.getTriggerType());
            if (ciTriggerType != null) {
                String triggerValue = metadata.getTriggerValue();
                switch (ciTriggerType) {
                    case REFS:
                        GitlabCiUtil.processTriggerRefs(ciJob, triggerValue);
                        break;
                    case EXACT_MATCH:
                        GitlabCiUtil.processExactMatch(ciJob, triggerValue);
                        break;
                    case REGEX_MATCH:
                        GitlabCiUtil.processRegexMatch(ciJob, triggerValue);
                        break;
                    case EXACT_EXCLUDE:
                        GitlabCiUtil.processExactExclude(ciJob, triggerValue);
                        break;
                }
            }
        }
    }

    private static MavenRepoVO convertRepo(NexusMavenRepoDTO nexusMavenRepoDTO) {
        MavenRepoVO mavenRepoVO = new MavenRepoVO();
        mavenRepoVO.setName(nexusMavenRepoDTO.getName());
        mavenRepoVO.setPrivateRepo(Boolean.TRUE);
        if ("MIXED".equals(nexusMavenRepoDTO.getVersionPolicy())) {
            mavenRepoVO.setType(GitOpsConstants.SNAPSHOT + "," + GitOpsConstants.RELEASE);
        } else {
            mavenRepoVO.setType(nexusMavenRepoDTO.getVersionPolicy().toLowerCase());
        }
        mavenRepoVO.setUrl(nexusMavenRepoDTO.getUrl());
        mavenRepoVO.setUsername(nexusMavenRepoDTO.getNeUserId());
        mavenRepoVO.setPassword(nexusMavenRepoDTO.getNeUserPassword());
        return mavenRepoVO;
    }

    /**
     * 生成maven构建相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param ciConfigTemplateVO maven构建阶段的信息
     * @param hasSettings        这个阶段是否有配置settings
     * @return 生成的shell脚本
     */
    private List<String> buildMavenScripts(final Long projectId, final Long jobId, CiConfigTemplateVO ciConfigTemplateVO, boolean hasSettings) {
        List<String> shells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(ciConfigTemplateVO.getScript()), true, true);
        if (hasSettings) {
            // 插入shell指令将配置的settings文件下载到项目目录下
            shells.add(0, GitlabCiUtil.downloadMavenSettings(projectId, jobId, ciConfigTemplateVO.getSequence()));
        }
        return shells;
    }

    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param jobVO          生成脚本
     * @return 生成的脚本列表
     */
    private List<String> buildScript(final Long organizationId, final Long projectId, DevopsCiJobVO jobVO) {
        Assert.notNull(jobVO, "Job can't be null");
        Assert.notNull(organizationId, "Organization id can't be null");
        Assert.notNull(projectId, "project id can't be null");
        final Long jobId = jobVO.getId();
        Assert.notNull(jobId, "Ci job id is required.");

        if (JobTypeEnum.SONAR.value().equals(jobVO.getType())) {
            // sonar配置转化为gitlab-ci配置
            List<String> scripts = new ArrayList<>();
            SonarQubeConfigVO sonarQubeConfigVO = JSONObject.parseObject(jobVO.getMetadata(), SonarQubeConfigVO.class);
            if (CiSonarConfigType.DEFAULT.value().equals(sonarQubeConfigVO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "error.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarCommand());
            } else if (CiSonarConfigType.CUSTOM.value().equals(sonarQubeConfigVO.getConfigType())) {
                if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
                    throw new CommonException("error.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getUsername(), sonarQubeConfigVO.getPassword()));
                } else if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getToken()));
                }
            } else {
                throw new CommonException("error.sonar.config.type.not.supported", sonarQubeConfigVO.getConfigType());
            }

            return scripts;
        } else if (JobTypeEnum.BUILD.value().equals(jobVO.getType())) {
            // 将构建类型的stage中的job的每个step进行解析和转化
            CiConfigVO ciConfigVO = JSONObject.parseObject(jobVO.getMetadata(), CiConfigVO.class);
            if (ciConfigVO == null || CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                return Collections.emptyList();
            }

            List<Long> existedSequences = new ArrayList<>();
            // 校验前端传入的sequence不为null且不重复
            ciConfigVO.getConfig().forEach(config -> DevopsCiPipelineAdditionalValidator.validConfigSequence(config.getSequence(), config.getName(), existedSequences));

            // 最后生成的所有script集合
            List<String> result = new ArrayList<>();

            // 同一个job中的所有step要按照sequence顺序来
            // 将每一个step都转为一个List<String>并将所有的list合并为一个
            ciConfigVO.getConfig()
                    .stream()
                    .sorted(Comparator.comparingLong(CiConfigTemplateVO::getSequence))
                    .forEach(config -> {
                        CiJobScriptTypeEnum type = CiJobScriptTypeEnum.forType(config.getType().toLowerCase());
                        if (type == null) {
                            throw new CommonException(ERROR_UNSUPPORTED_STEP_TYPE, config.getType());
                        }

                        switch (type) {
                            // GO和NPM是一样处理
                            case NPM:
                                result.addAll(GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(config.getScript()), true, true));
                                break;
                            case MAVEN:
                                // 处理settings文件
                                DevopsCiPipelineAdditionalValidator.validateMavenStep(config);
                                boolean hasSettings = buildAndSaveMavenSettings(projectId, jobId, config);
                                result.addAll(buildMavenScripts(projectId, jobId, config, hasSettings));
                                break;
                            case DOCKER:
                                // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
                                result.add(GitlabCiUtil.generateDockerScripts(
                                        config.getDockerContextDir(),
                                        config.getDockerFilePath(),
                                        config.getSkipDockerTlsVerify() == null || config.getSkipDockerTlsVerify()));
                                break;
                            case MAVEN_DEPLOY:
                                List<MavenRepoVO> repos = buildAndSaveMavenSettings(projectId, jobId, config.getSequence(), config.getMavenDeployRepoSettings());
                                result.addAll(buildMavenScripts(projectId, jobId, config, repos));
                                break;
                        }
                    });

            return result;
        } else if (JobTypeEnum.CHART.value().equals(jobVO.getType())) {
            // 生成chart步骤
            return ArrayUtil.singleAsList(GitlabCiUtil.generateChartBuildScripts());
        }
        return Collections.emptyList();
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param ciConfigTemplateVO 配置信息
     * @return true表示有settings配置，false表示没有
     */
    private boolean buildAndSaveMavenSettings(Long projectId, Long jobId, CiConfigTemplateVO ciConfigTemplateVO) {
        // settings文件内容
        String settings;
        final List<MavenRepoVO> repos = new ArrayList<>();

        // 是否有手动填写仓库表单
        final boolean hasManualRepos = !CollectionUtils.isEmpty(ciConfigTemplateVO.getRepos());
        // 是否有选择已有的maven仓库
        final boolean hasNexusRepos = !CollectionUtils.isEmpty(ciConfigTemplateVO.getNexusMavenRepoIds());

        if (!StringUtils.isEmpty(ciConfigTemplateVO.getMavenSettings())) {
            // 使用用户提供的xml内容，不进行内容的校验
            settings = Base64Util.getBase64DecodedString(ciConfigTemplateVO.getMavenSettings());
        } else if (hasManualRepos || hasNexusRepos) {
            if (hasNexusRepos) {
                // 用户选择的已有的maven仓库
                List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, ciConfigTemplateVO.getNexusMavenRepoIds());
                repos.addAll(nexusMavenRepoDTOs.stream().map(DevopsCiPipelineServiceImpl::convertRepo).collect(Collectors.toList()));
            }

            if (hasManualRepos) {
                // 由用户填写的表单构建xml文件内容
                repos.addAll(ciConfigTemplateVO.getRepos());
            }

            // 构建settings文件
            settings = buildSettings(repos);
        } else {
            // 没有填关于settings的信息
            return false;
        }

        // 这里存储的ci setting文件内容是解密后的
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, ciConfigTemplateVO.getSequence(), settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return true;
    }

    @Nullable
    private Cache buildJobCache(DevopsCiJobVO jobConfig) {
        boolean isToUpload = Boolean.TRUE.equals(jobConfig.getToUpload());
        boolean isToDownload = Boolean.TRUE.equals(jobConfig.getToDownload());
        if (isToUpload && isToDownload) {
            return constructCache(CachePolicy.PULL_PUSH.getValue());
        } else if (isToDownload) {
            return constructCache(CachePolicy.PULL.getValue());
        } else if (isToUpload) {
            return constructCache(CachePolicy.PUSH.getValue());
        } else {
            return null;
        }
    }

    private Cache constructCache(String policy) {
        Cache cache = new Cache();
        cache.setKey(GitOpsConstants.GITLAB_CI_DEFAULT_CACHE_KEY);
        cache.setPaths(Collections.singletonList(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
        cache.setPolicy(policy);
        return cache;
    }

    private void buildBeforeScript(GitlabCi gitlabCi) {
        List<String> beforeScripts = ArrayUtil.singleAsList(GitOpsConstants.CHOERODON_BEFORE_SCRIPT);
        // 如果有job启用了缓存设置, 就创建缓存目录
        if (!CollectionUtils.isEmpty(gitlabCi.getJobs())){
            if (gitlabCi.getJobs().values().stream().anyMatch(j -> j.getCache() != null)) {
                beforeScripts.add(GitlabCiUtil.generateCreateCacheDir(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
            }
            gitlabCi.setBeforeScript(beforeScripts);
        }
    }

    /**
     * 生成maven构建相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param ciConfigTemplateVO maven发布软件包阶段的信息
     * @param mavenRepoVO        仓库信息
     * @return 生成的shell脚本
     */
    private List<String> buildMavenScripts(final Long projectId, final Long jobId, CiConfigTemplateVO ciConfigTemplateVO, List<MavenRepoVO> mavenRepoVO) {
        List<String> shells = new ArrayList<>();
        // 这里这么写是为了考虑之后可能选了多个仓库, 如果是多个仓库的话, 变量替换不便
        List<String> templateShells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(ciConfigTemplateVO.getScript()), true, true);
        if (!CollectionUtils.isEmpty(mavenRepoVO)) {
            // 插入shell指令将配置的settings文件下载到项目目录下
            shells.add(GitlabCiUtil.downloadMavenSettings(projectId, jobId, ciConfigTemplateVO.getSequence()));

            // 包含repoId锚点的字符串在templateShells中的索引号
            int repoIdIndex = -1;
            // 包含repoUrl锚点的字符串在templateShells中的索引号
            int repoUrlIndex = -1;
            // 寻找包含这两个锚点的字符串位置
            for (int i = 0; i < templateShells.size(); i++) {
                if (repoIdIndex == -1) {
                    if (templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_ID)) {
                        repoIdIndex = i;
                    }
                }
                if (repoUrlIndex == -1) {
                    if (templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_URL)) {
                        repoUrlIndex = i;
                    }
                }
                if (repoIdIndex != -1 && repoUrlIndex != -1) {
                    // 没必要再找了
                    break;
                }
            }

            // 为每一个仓库都从模板的脚本中加一份生成的命令
            for (MavenRepoVO repo : mavenRepoVO) {
                // 将预定的变量(仓库名和地址)替换为settings.xml文件指定的
                List<String> commands = new ArrayList<>(templateShells);
                if (repoIdIndex != -1) {
                    commands.set(repoIdIndex, commands.get(repoIdIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_ID, repo.getName()));
                }
                if (repoUrlIndex != -1) {
                    commands.set(repoUrlIndex, commands.get(repoUrlIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_URL, repo.getUrl()));
                }
                shells.addAll(commands);
            }
        } else {
            shells.addAll(templateShells);
        }
        return shells;
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId               项目id
     * @param jobId                   job id
     * @param sequence                这个步骤的序号
     * @param mavenDeployRepoSettings 配置信息
     * @return 为空表示没有settings配置，不为空表示有
     */
    private List<MavenRepoVO> buildAndSaveMavenSettings(Long projectId, Long jobId, Long sequence, MavenDeployRepoSettings mavenDeployRepoSettings) {
        if (CollectionUtils.isEmpty(mavenDeployRepoSettings.getNexusRepoIds())) {
            return Collections.emptyList();
        }
        List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, mavenDeployRepoSettings.getNexusRepoIds());

        if (CollectionUtils.isEmpty(nexusMavenRepoDTOs)) {
            return Collections.emptyList();
        }

        List<MavenRepoVO> mavenRepoVOS = nexusMavenRepoDTOs.stream().map(DevopsCiPipelineServiceImpl::convertRepo).collect(Collectors.toList());

        // settings文件内容
        String settings = buildSettings(mavenRepoVOS);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, sequence, settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return mavenRepoVOS;
    }

    private void createUserRel(List<Long> cdAuditUserIds, Long pipelineId, Long stageId, Long jobId) {
        if (cdAuditUserIds != null && cdAuditUserIds.size() > 0) {
            cdAuditUserIds.forEach(t -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO(pipelineId, stageId, jobId);
                devopsCdAuditDTO.setUserId(t);
                devopsCdAuditService.baseCreate(devopsCdAuditDTO);
            });
        }
    }

    private void createCdJob(DevopsCdJobVO t, Long projectId, Long stageId, Long pipelineId) {
        t.setProjectId(projectId);
        t.setStageId(stageId);
        t.setPipelineId(pipelineId);
        Long jobId = devopsCdJobService.create(ConvertUtils.convertObject(t, DevopsCdJobDTO.class)).getId();
        if (JobTypeEnum.CD_AUDIT.value().equals(t.getType())) {
            createUserRel(t.getCdAuditUserIds(), null, null, jobId);
        }
    }

    private PipelineAppServiceDeployDTO deployVoToDto(PipelineAppServiceDeployVO appServiceDeployVO) {
        PipelineAppServiceDeployDTO appServiceDeployDTO = new PipelineAppServiceDeployDTO();
        BeanUtils.copyProperties(appServiceDeployVO, appServiceDeployDTO);
        if (appServiceDeployVO.getTriggerVersion() != null && !appServiceDeployVO.getTriggerVersion().isEmpty()) {
            appServiceDeployDTO.setTriggerVersion(String.join(",", appServiceDeployVO.getTriggerVersion()));
        }
        return appServiceDeployDTO;
    }

    public String getFromBASE64(String s) {
        if (s == null) return null;
        try {
            byte[] bytes = s.getBytes("utf-8");
            byte[] decode = Base64.getDecoder().decode(bytes);
            return new String(decode);
        } catch (Exception e) {
            return null;
        }
    }
}
