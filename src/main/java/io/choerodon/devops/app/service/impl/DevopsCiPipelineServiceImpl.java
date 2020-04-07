package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.ci.CiJob;
import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi;
import io.choerodon.devops.infra.dto.gitlab.ci.Include;
import io.choerodon.devops.infra.dto.gitlab.ci.OnlyExceptPolicy;
import io.choerodon.devops.infra.enums.DefaultTriggerRefTypeEnum;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitlabCiUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/2 18:00
 */
@Service
public class DevopsCiPipelineServiceImpl implements DevopsCiPipelineService {

    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";
    private static final String ERROR_USER_HAVE_NO_APP_PERMISSION = "error.user.have.no.app.permission";
    @Value("${services.gateway.url}")
    private String gatewayUrl;

    private DevopsCiPipelineMapper devopsCiPipelineMapper;
    private DevopsCiStageService devopsCiStageService;
    private DevopsCiJobService devopsCiJobService;
    private DevopsCiContentService devopsCiContentService;
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private UserAttrService userAttrService;
    private PermissionHelper permissionHelper;
    private AppServiceService appServiceService;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public DevopsCiPipelineServiceImpl(DevopsCiPipelineMapper devopsCiPipelineMapper, DevopsCiStageService devopsCiStageService, DevopsCiJobService devopsCiJobService, DevopsCiContentService devopsCiContentService, GitlabServiceClientOperator gitlabServiceClientOperator, UserAttrService userAttrService, PermissionHelper permissionHelper, AppServiceService appServiceService) {
        this.devopsCiPipelineMapper = devopsCiPipelineMapper;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
        this.devopsCiContentService = devopsCiContentService;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.permissionHelper = permissionHelper;
        this.appServiceService = appServiceService;
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO create(Long projectId, DevopsCiPipelineVO devopsCiPipelineVO) {
        Long iamUserId = TypeUtil.objToLong(GitUserNameUtil.getUserId());
        checkUserPermission(devopsCiPipelineVO.getAppServiceId(),iamUserId);
        devopsCiPipelineVO.setProjectId(projectId);
        DevopsCiPipelineDTO devopsCiPipelineDTO = ConvertUtils.convertObject(devopsCiPipelineVO, DevopsCiPipelineDTO.class);
        if (devopsCiPipelineMapper.insertSelective(devopsCiPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }

        // 保存stage信息
        devopsCiPipelineVO.getStageList().forEach(devopsCiStageVO -> {
            DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
            devopsCiStageDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);

            // 保存job信息
            devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                devopsCiJobDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
                devopsCiJobDTO.setStageId(savedDevopsCiStageDTO.getId());
                devopsCiJobService.create(devopsCiJobDTO);
            });
        });

        // TODO 保存ci配置文件
        saveCiContent(devopsCiPipelineDTO.getId(), devopsCiPipelineVO);

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(devopsCiPipelineDTO.getAppServiceId());
        String ciFileIncludeUrl = gatewayUrl + "/devops/v1/projects/" + projectId + "/ci_contents/pipelines/" + devopsCiPipelineDTO.getId();
        initGitlabCiFile(appServiceDTO.getGitlabProjectId(),ciFileIncludeUrl ,iamUserId);
        return devopsCiPipelineMapper.selectByPrimaryKey(devopsCiPipelineDTO.getId());
    }

    private void checkUserPermission(Long appServiceId, Long iamUserId) {
        if (!appServiceService.checkAppSerivcePermissionForUser(appServiceId, iamUserId)) {
            throw new CommonException(ERROR_USER_HAVE_NO_APP_PERMISSION);
        }

    }

    /**
     * 第一次创建CI流水线时初始化仓库下的.gitlab-ci.yml文件
     *
     * @param gitlabProjectId  gitlab项目id
     * @param ciFileIncludeUrl include中的链接
     * @param iamUserId      iam用户id
     */
    private void initGitlabCiFile(Integer gitlabProjectId, String ciFileIncludeUrl, Long iamUserId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, GitOpsConstants.MASTER, GitOpsConstants.GITLAB_CI_FILE_NAME);

        if (repositoryFile == null) {
            // 说明项目下还没有CI文件
            gitlabServiceClientOperator.createFile(
                    gitlabProjectId,
                    GitOpsConstants.GITLAB_CI_FILE_NAME,
                    buildIncludeYaml(ciFileIncludeUrl),
                    GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        } else {
            // TODO 更新要不要将原先的配置文件注释并放在原本文件中
//            String originFileContent = new String(Base64.getDecoder().decode(repositoryFile.getContent().getBytes()), Charset.forName("UTF-8"));
            // 将原本内容覆盖
            gitlabServiceClientOperator.updateFile(
                    gitlabProjectId,
                    GitOpsConstants.GITLAB_CI_FILE_NAME,
                    buildIncludeYaml(ciFileIncludeUrl),
                    GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO update(Long projectId, Long ciPipelineId, DevopsCiPipelineVO devopsCiPipelineVO) {
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
                devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                    DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                    devopsCiJobService.create(devopsCiJobDTO);
                });
            } else {
                // 新增
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存job信息
                devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                    DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                    devopsCiJobDTO.setStageId(savedDevopsCiStageDTO.getId());
                    devopsCiJobService.create(devopsCiJobDTO);
                });
            }
        });
        // TODO 更新ci配置

        return devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }

    private static String buildIncludeYaml(String ciFileIncludeUrl) {
        GitlabCi gitlabCi = new GitlabCi();
        Include include = new Include();
        include.setRemote(Objects.requireNonNull(ciFileIncludeUrl));
        gitlabCi.setInclude(ArrayUtil.singleAsList(include));
        return GitlabCiUtil.gitlabCi2yaml(gitlabCi);
    }

    private void saveCiContent(Long pipelineId, DevopsCiPipelineVO devopsCiPipelineVO) {
        GitlabCi gitlabCi = buildGitLabCiObject(devopsCiPipelineVO);
        String gitlabCiYaml = GitlabCiUtil.gitlabCi2yaml(gitlabCi);
        //保存gitlab-ci配置文件
        DevopsCiContentDTO devopsCiContentDTO = new DevopsCiContentDTO();
        devopsCiContentDTO.setCiPipelineId(pipelineId);
        devopsCiContentDTO.setCiContentFile(gitlabCiYaml);
        devopsCiContentService.create(devopsCiContentDTO);
    }

    private GitlabCi buildGitLabCiObject(DevopsCiPipelineVO devopsCiPipelineVO) {
        List<String> stages = devopsCiPipelineVO.getStageList().stream()
                .sorted(Comparator.comparing(DevopsCiStageVO::getId))
                .map(DevopsCiStageVO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();
        // 先使用默认的image,后面可以考虑让用户自己指定
        gitlabCi.setImage("registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.9.1");
        gitlabCi.setStages(stages);
        devopsCiPipelineVO.getStageList().forEach(stageVO ->
            stageVO.getJobList().forEach(jobV0 -> {
                CiJob ciJob = new CiJob();
                ciJob.setStage(stageVO.getName());
                ciJob.setOnly(buildOnlyExceptPolicyObject(jobV0.getTriggerRefs()));
                ciJob.setScript(calculateScript(jobV0));
                // todo except && cache未配置
                gitlabCi.addJob(jobV0.getName(), ciJob);
            })
        );
        return gitlabCi;
    }
    // todo 待处理
    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     * @param jobV0
     * @return
     */
    private List<String> calculateScript(DevopsCiJobVO jobV0) {

        return null;
    }

    private OnlyExceptPolicy buildOnlyExceptPolicyObject(String triggerRefs) {
        OnlyExceptPolicy onlyExceptPolicy = new OnlyExceptPolicy();
        List<String> refs = new ArrayList<>();
        for (String ref : triggerRefs.split(",")) {
            if (!DefaultTriggerRefTypeEnum.contains(ref)) {
                ref = "/^.*" + ref + ".*$/";
            }
            refs.add(ref);
        }
        onlyExceptPolicy.setRefs(refs);
        return onlyExceptPolicy;
    }
}
