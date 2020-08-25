package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.GitOpsConstants.DEFAULT_PIPELINE_RECORD_SIZE;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineMapper;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineRecordMapper;
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

    private DevopsCiPipelineMapper devopsCiPipelineMapper;
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    private DevopsCiStageService devopsCiStageService;
    private DevopsCiJobService devopsCiJobService;
    private DevopsCiContentService devopsCiContentService;
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private UserAttrService userAttrService;
    private AppServiceService appServiceService;
    private DevopsCiJobRecordService devopsCiJobRecordService;
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    private DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private DevopsProjectService devopsProjectService;
    private BaseServiceClientOperator baseServiceClientOperator;
    private PermissionHelper permissionHelper;

    public DevopsCiPipelineServiceImpl(
            @Lazy DevopsCiPipelineMapper devopsCiPipelineMapper,
            // 这里的懒加载是为了避免循环依赖
            @Lazy DevopsCiPipelineRecordService devopsCiPipelineRecordService,
            DevopsCiStageService devopsCiStageService,
            @Lazy DevopsCiJobService devopsCiJobService,
            DevopsCiContentService devopsCiContentService,
            GitlabServiceClientOperator gitlabServiceClientOperator,
            UserAttrService userAttrService,
            @Lazy
                    AppServiceService appServiceService,
            DevopsCiJobRecordService devopsCiJobRecordService,
            DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper,
            DevopsProjectService devopsProjectService,
            BaseServiceClientOperator baseServiceClientOperator,
            PermissionHelper permissionHelper,
            DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper) {
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
        this.permissionHelper = permissionHelper;
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
    public DevopsCiPipelineDTO create(Long projectId, DevopsCiPipelineVO devopsCiPipelineVO) {
        Long iamUserId = TypeUtil.objToLong(GitUserNameUtil.getUserId());
        checkUserPermission(devopsCiPipelineVO.getAppServiceId(), iamUserId);
        checkNonCiPipelineBefore(devopsCiPipelineVO.getAppServiceId());
        devopsCiPipelineVO.setProjectId(projectId);

        // 设置默认镜像
        if (StringUtils.isEmpty(devopsCiPipelineVO.getImage())) {
            devopsCiPipelineVO.setImage(defaultCiImage);
        }

        DevopsCiPipelineDTO devopsCiPipelineDTO = ConvertUtils.convertObject(devopsCiPipelineVO, DevopsCiPipelineDTO.class);
        devopsCiPipelineDTO.setToken(GenerateUUID.generateUUID());
        if (devopsCiPipelineMapper.insertSelective(devopsCiPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }

        // 保存stage信息
        devopsCiPipelineVO.getStageList().forEach(devopsCiStageVO -> {
            DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
            devopsCiStageDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);

            // 保存job信息
            if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                    // 不让数据库存加密的值
                    processCiJobVO(devopsCiJobVO);

                    DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);

                    devopsCiJobDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
                    devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                    devopsCiJobVO.setId(devopsCiJobService.create(devopsCiJobDTO).getId());
                });
            }

        });

        // 保存ci配置文件
        saveCiContent(projectId, devopsCiPipelineDTO.getId(), devopsCiPipelineVO);

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(devopsCiPipelineDTO.getAppServiceId());
        String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, devopsCiPipelineDTO.getToken());
        initGitlabCiFile(appServiceDTO.getGitlabProjectId(), ciFileIncludeUrl);
        return devopsCiPipelineMapper.selectByPrimaryKey(devopsCiPipelineDTO.getId());
    }

    @Override
    public DevopsCiPipelineVO query(Long projectId, Long ciPipelineId) {
        // 根据pipeline_id查询数据
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
        CommonExAssertUtil.assertTrue(devopsCiPipelineDTO != null, "error.ci.pipeline.not.exist", ciPipelineId);
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(ciPipelineId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(ciPipelineId);
        // dto转vo
        DevopsCiPipelineVO devopsCiPipelineVO = ConvertUtils.convertObject(devopsCiPipelineDTO, DevopsCiPipelineVO.class);
        List<DevopsCiStageVO> devopsCiStageVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageVO.class);
        List<DevopsCiJobVO> devopsCiJobVOS = ConvertUtils.convertList(devopsCiJobDTOS, DevopsCiJobVO.class);
        devopsCiJobVOS.forEach(this::processBeforeQueryJob);

        // 封装对象
        Map<Long, List<DevopsCiJobVO>> jobMap = devopsCiJobVOS.stream().collect(Collectors.groupingBy(DevopsCiJobVO::getCiStageId));
        devopsCiStageVOS.forEach(devopsCiStageVO -> {
            List<DevopsCiJobVO> ciJobVOS = jobMap.getOrDefault(devopsCiStageVO.getId(), Collections.emptyList());
            ciJobVOS.sort(Comparator.comparingLong(DevopsCiJobVO::getId));
            devopsCiStageVO.setJobList(ciJobVOS);
        });
        // stage排序
        devopsCiStageVOS = devopsCiStageVOS.stream().sorted(Comparator.comparing(DevopsCiStageVO::getSequence)).collect(Collectors.toList());
        devopsCiPipelineVO.setStageList(devopsCiStageVOS);

        return devopsCiPipelineVO;
    }

    private void processBeforeQueryJob(DevopsCiJobVO devopsCiJobVO) {
        if (CiJobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
            // 反序列化
            CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobVO.getMetadata(), CiConfigVO.class);
            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                // 将script字段加密
                ciConfigVO.getConfig().forEach(c -> c.setScript(Base64Util.getBase64EncodedString(c.getScript())));
                // 序列化
                devopsCiJobVO.setMetadata(JsonHelper.singleQuoteJson(JSONObject.toJSONString(ciConfigVO)));
            }
        } else if (CiJobTypeEnum.CUSTOM.value().equals(devopsCiJobVO.getType())){
            // 加密自定义任务的元数据
            devopsCiJobVO.setMetadata(Base64Util.getBase64EncodedString(devopsCiJobVO.getMetadata()));
        }
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
    public List<DevopsCiPipelineVO> listByProjectIdAndAppName(Long projectId, String name) {
        if (projectId == null) {
            throw new CommonException(ERROR_PROJECT_ID_IS_NULL);
        }
        boolean isOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);
        List<DevopsCiPipelineVO> devopsCiPipelineVOS;
        if (isOwner) {
            devopsCiPipelineVOS = devopsCiPipelineMapper.queryByProjectIdAndName(projectId, name);
        } else {
            devopsCiPipelineVOS = devopsCiPipelineMapper.queryByProjectIdAndNameForProjectMember(projectId, DetailsHelper.getUserDetails().getUserId(), name);
        }
        PageRequest pageable = new PageRequest(GitOpsConstants.FIRST_PAGE_INDEX, DEFAULT_PIPELINE_RECORD_SIZE, new Sort(new Sort.Order(Sort.Direction.DESC, DevopsCiPipelineRecordDTO.FIELD_GITLAB_PIPELINE_ID)));

        devopsCiPipelineVOS.forEach(devopsCiPipelineVO -> {
            Page<DevopsCiPipelineRecordVO> pipelineRecordVOPageInfo = devopsCiPipelineRecordService.pagingPipelineRecord(projectId, devopsCiPipelineVO.getId(), pageable);
            if (!CollectionUtils.isEmpty(pipelineRecordVOPageInfo.getContent())) {
                devopsCiPipelineVO.setLatestExecuteDate(pipelineRecordVOPageInfo.getContent().get(0).getCreatedDate());
                devopsCiPipelineVO.setLatestExecuteStatus(pipelineRecordVOPageInfo.getContent().get(0).getStatus());
            }
            devopsCiPipelineVO.setPipelineRecordVOList(pipelineRecordVOPageInfo.getContent());
            devopsCiPipelineVO.setHasMoreRecords(pipelineRecordVOPageInfo.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
        });

        return devopsCiPipelineVOS;
    }

    /**
     * 校验应用服务之前并不存在流水线
     *
     * @param appServiceId 应用服务id
     */
    private void checkNonCiPipelineBefore(Long appServiceId) {
        if (countByAppServiceId(appServiceId) > 0) {
            throw new CommonException("error.ci.pipeline.exists.for.app.service", appServiceId);
        }
    }

    private int countByAppServiceId(Long appServiceId) {
        DevopsCiPipelineDTO devopsCiPipelineDTO = new DevopsCiPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(Objects.requireNonNull(appServiceId));
        return devopsCiPipelineMapper.selectCount(devopsCiPipelineDTO);
    }

    @Override
    public DevopsCiPipelineVO queryById(Long ciPipelineId) {
        return devopsCiPipelineMapper.queryById(ciPipelineId);
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO disablePipeline(Long projectId, Long ciPipelineId) {
        if (devopsCiPipelineMapper.disablePipeline(ciPipelineId) != 1) {
            throw new CommonException(DISABLE_PIPELINE_FAILED);
        }
        return devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }

    @Override
    @Transactional
    public void deletePipeline(Long projectId, Long ciPipelineId) {
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(devopsCiPipelineDTO.getAppServiceId());
        // 校验用户是否有应用服务权限
        Long userId = DetailsHelper.getUserDetails().getUserId();
        checkUserPermission(appServiceDTO.getId(), userId);
        // 删除流水线
        if (devopsCiPipelineMapper.deleteByPrimaryKey(ciPipelineId) != 1) {
            throw new CommonException(DELETE_PIPELINE_FAILED);
        }
        // 删除stage
        devopsCiStageService.deleteByPipelineId(ciPipelineId);

        // 删除job
        devopsCiJobService.deleteByPipelineId(ciPipelineId);

        // 删除job记录
        devopsCiJobRecordService.deleteByGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());

        // 删除pipeline记录
        devopsCiPipelineRecordService.deleteByGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());

        // 删除content file
        devopsCiContentService.deleteByPipelineId(ciPipelineId);

        // 删除.gitlab-ci.yaml文件
        deleteGitlabCiFile(appServiceDTO.getGitlabProjectId());
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO enablePipeline(Long projectId, Long ciPipelineId) {
        if (devopsCiPipelineMapper.enablePipeline(ciPipelineId) != 1) {
            throw new CommonException(ENABLE_PIPELINE_FAILED);
        }
        return devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }

    @Override
    public void executeNew(Long projectId, Long ciPipelineId, Long gitlabProjectId, String ref) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, userAttrDTO.getGitlabUserId(), gitlabProjectId, ref);
        Pipeline pipeline = gitlabServiceClientOperator.createPipeline(gitlabProjectId.intValue(), userAttrDTO.getGitlabUserId().intValue(), ref);
        // 保存执行记录
        try {
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.create(ciPipelineId, gitlabProjectId, pipeline);
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(), pipeline.getId(), userAttrDTO.getGitlabUserId().intValue());
            devopsCiJobRecordService.create(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTOS, userAttrDTO.getIamUserId());
        } catch (Exception e) {
            LOGGER.info("save pipeline Records failed， ciPipelineId {}.", ciPipelineId);
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
    public DevopsCiPipelineDTO update(Long projectId, Long ciPipelineId, DevopsCiPipelineVO devopsCiPipelineVO) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        checkUserPermission(devopsCiPipelineVO.getAppServiceId(), userId);
        // 校验自定义任务格式
        DevopsCiPipelineDTO devopsCiPipelineDTO = ConvertUtils.convertObject(devopsCiPipelineVO, DevopsCiPipelineDTO.class);
        devopsCiPipelineDTO.setId(ciPipelineId);
        if (devopsCiPipelineMapper.updateByPrimaryKeySelective(devopsCiPipelineDTO) != 1) {
            throw new CommonException(UPDATE_PIPELINE_FAILED);
        }
        // 更新stage
        // 查询数据库中原有stage列表,并和新的stage列表作比较。
        // 差集：要删除的记录
        // 交集：要更新的记录
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(ciPipelineId);
        Set<Long> oldStageIds = devopsCiStageDTOList.stream().map(DevopsCiStageDTO::getId).collect(Collectors.toSet());

        Set<Long> updateIds = devopsCiPipelineVO.getStageList().stream()
                .filter(devopsCiStageVO -> devopsCiStageVO.getId() != null)
                .map(DevopsCiStageVO::getId)
                .collect(Collectors.toSet());
        // 去掉要更新的记录，剩下的为要删除的记录
        oldStageIds.removeAll(updateIds);
        oldStageIds.forEach(stageId -> {
            devopsCiStageService.deleteById(stageId);
            devopsCiJobService.deleteByStageId(stageId);
        });

        devopsCiPipelineVO.getStageList().forEach(devopsCiStageVO -> {
            if (devopsCiStageVO.getId() != null) {
                // 更新
                devopsCiStageService.update(devopsCiStageVO);
                devopsCiJobService.deleteByStageId(devopsCiStageVO.getId());
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        processCiJobVO(devopsCiJobVO);

                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setId(null);
                        devopsCiJobDTO.setCiStageId(devopsCiStageVO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
                        devopsCiJobService.create(devopsCiJobDTO);
                        devopsCiJobVO.setId(devopsCiJobDTO.getId());
                    });
                }
            } else {
                // 新增
                devopsCiStageVO.setCiPipelineId(ciPipelineId);
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        processCiJobVO(devopsCiJobVO);

                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
                        devopsCiJobService.create(devopsCiJobDTO);
                        devopsCiJobVO.setId(devopsCiJobDTO.getId());
                    });
                }
            }
        });
        saveCiContent(projectId, ciPipelineId, devopsCiPipelineVO);
        return devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }

    private void processCiJobVO(DevopsCiJobVO devopsCiJobVO) {
        // 不让数据库存加密的值
        if (CiJobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
            // 将构建类型的stage中的job的每个step进行解析和转化
            CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobVO.getMetadata(), CiConfigVO.class);
            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                ciConfigVO.getConfig().forEach(c -> {
                    if (!org.springframework.util.StringUtils.isEmpty(c.getScript())) {
                        c.setScript(Base64Util.getBase64DecodedString(c.getScript()));
                    }
                });
            }
            devopsCiJobVO.setConfigVO(ciConfigVO);
            devopsCiJobVO.setMetadata(JSONObject.toJSONString(ciConfigVO));
        }
    }

    private void saveCiContent(final Long projectId, Long pipelineId, DevopsCiPipelineVO devopsCiPipelineVO) {
        GitlabCi gitlabCi = buildGitLabCiObject(projectId, devopsCiPipelineVO);
        StringBuilder gitlabCiYaml = new StringBuilder(GitlabCiUtil.gitlabCi2yaml(gitlabCi));

        // 拼接自定义job
        if (!CollectionUtils.isEmpty(devopsCiPipelineVO.getStageList())) {
            List<DevopsCiJobVO> ciJobVOS = devopsCiPipelineVO.getStageList().stream()
                    .flatMap(v -> v.getJobList().stream()).filter(job -> CiJobTypeEnum.CUSTOM.value().equalsIgnoreCase(job.getType()))
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
     * @param projectId          项目id
     * @param devopsCiPipelineVO 流水线数据
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(final Long projectId, DevopsCiPipelineVO devopsCiPipelineVO) {
        // 对阶段排序
        List<String> stages = devopsCiPipelineVO.getStageList().stream()
                .sorted(Comparator.comparing(DevopsCiStageVO::getSequence))
                .map(DevopsCiStageVO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();

        // 如果用户指定了就使用用户指定的，如果没有指定就使用默认的猪齿鱼提供的镜像
        gitlabCi.setImage(StringUtils.isEmpty(devopsCiPipelineVO.getImage()) ? defaultCiImage : devopsCiPipelineVO.getImage());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        gitlabCi.setStages(stages);
        devopsCiPipelineVO.getStageList().forEach(stageVO -> {
            if (!CollectionUtils.isEmpty(stageVO.getJobList())) {
                stageVO.getJobList().forEach(job -> {
                    if (CiJobTypeEnum.CUSTOM.value().equals(job.getType())) {
                        return;
                    }
                    CiJob ciJob = new CiJob();
                    if (!StringUtils.isEmpty(job.getImage())) {
                        ciJob.setImage(job.getImage());
                    }
                    ciJob.setStage(stageVO.getName());
                    ciJob.setOnly(buildOnlyExceptPolicyObject(job.getTriggerRefs()));
                    ciJob.setScript(buildScript(Objects.requireNonNull(projectDTO.getOrganizationId()), projectId, job));
                    ciJob.setCache(buildJobCache(job));
                    gitlabCi.addJob(job.getName(), ciJob);
                });
            }
        });
        buildBeforeScript(gitlabCi);
        return gitlabCi;
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

        if (CiJobTypeEnum.SONAR.value().equals(jobVO.getType())) {
            // sonar配置转化为gitlab-ci配置
            List<String> scripts = new ArrayList<>();
            SonarQubeConfigVO sonarQubeConfigVO = JSONObject.parseObject(jobVO.getMetadata(), SonarQubeConfigVO.class);
            if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
                throw new CommonException("error.sonar.url.is.null");
            }
            if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
                scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getUsername(), sonarQubeConfigVO.getPassword()));
            } else if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
                scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getToken()));
            }
            return scripts;
        } else if (CiJobTypeEnum.BUILD.value().equals(jobVO.getType())) {
            // 取出之前反序列化的值
            CiConfigVO ciConfigVO = jobVO.getConfigVO();
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
                                boolean hasSettings = buildAndSaveMavenSettings(jobId, config);
                                result.addAll(buildMavenScripts(projectId, jobId, config, hasSettings));
                                break;
                            case DOCKER:
                                // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
                                // TODO 修复 目前后端这个参数的含义是是否跳过证书校验, 前端的含义是是否进行证书校验
                                Boolean doTlsVerify = config.getSkipDockerTlsVerify();
                                result.add(GitlabCiUtil.generateDockerScripts(
                                        config.getDockerContextDir(),
                                        config.getDockerFilePath(),
                                        doTlsVerify == null || !doTlsVerify));
                                break;
                        }
                    });

            return result;
        } else if (CiJobTypeEnum.CHART.value().equals(jobVO.getType())) {
            // 生成chart步骤
            return ArrayUtil.singleAsList(GitlabCiUtil.generateChartBuildScripts());
        }
        return Collections.emptyList();
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
     * 生成并存储maven settings到数据库
     *
     * @param jobId              job id
     * @param ciConfigTemplateVO 配置信息
     * @return true表示有settings配置，false表示没有
     */
    private boolean buildAndSaveMavenSettings(Long jobId, CiConfigTemplateVO ciConfigTemplateVO) {
        // settings文件内容
        String settings;
        if (!CollectionUtils.isEmpty(ciConfigTemplateVO.getRepos())) {
            // 由用户填写的表单构建xml文件内容
            settings = buildSettings(ciConfigTemplateVO.getRepos());
        } else if (!StringUtils.isEmpty(ciConfigTemplateVO.getMavenSettings())) {
            // 使用用户提供的xml内容，不进行内容的校验
            settings = Base64Util.getBase64DecodedString(ciConfigTemplateVO.getMavenSettings());
        } else {
            // 用户没有提供settings文件配置
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
        // 如果全部都是自定义任务, 这个map是空的
        if (!CollectionUtils.isEmpty(gitlabCi.getJobs())) {
            if (gitlabCi.getJobs().values().stream().anyMatch(j -> j.getCache() != null)) {
                beforeScripts.add(GitlabCiUtil.generateCreateCacheDir(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
            }
        }
        gitlabCi.setBeforeScript(beforeScripts);
    }

    private OnlyExceptPolicy buildOnlyExceptPolicyObject(String triggerRefs) {
        OnlyExceptPolicy onlyExceptPolicy = new OnlyExceptPolicy();
        List<String> refs = new ArrayList<>();
        for (String ref : triggerRefs.split(",")) {
            if (!DefaultTriggerRefTypeEnum.contains(ref)) {
                if ("tag".equals(ref)) {
                    ref = DefaultTriggerRefTypeEnum.TAGS.value();
                } else {
                    ref = "/^.*" + ref + ".*$/";
                }

            }

            refs.add(ref);
        }
        onlyExceptPolicy.setRefs(refs);
        return onlyExceptPolicy;
    }
}
