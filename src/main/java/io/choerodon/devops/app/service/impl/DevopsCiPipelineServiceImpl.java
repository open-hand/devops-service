package io.choerodon.devops.app.service.impl;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.ci.CiJob;
import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi;
import io.choerodon.devops.infra.dto.gitlab.ci.OnlyExceptPolicy;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.DefaultTriggerRefTypeEnum;
import io.choerodon.devops.infra.enums.SonarAuthType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineMapper;
import io.choerodon.devops.infra.util.*;

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
    private static final String ERROR_APP_SVC_ID_IS_NULL = "error.app.svc.id.is.null";
    private static final String ERROR_PROJECT_ID_IS_NULL = "error.project.id.is.null";
    @Value("${services.gateway.url}")
    private String gatewayUrl;

    private DevopsCiPipelineMapper devopsCiPipelineMapper;
    private DevopsCiStageService devopsCiStageService;
    private DevopsCiJobService devopsCiJobService;
    private DevopsCiContentService devopsCiContentService;
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private UserAttrService userAttrService;
    private AppServiceService appServiceService;

    public DevopsCiPipelineServiceImpl(DevopsCiPipelineMapper devopsCiPipelineMapper, DevopsCiStageService devopsCiStageService, DevopsCiJobService devopsCiJobService, DevopsCiContentService devopsCiContentService, GitlabServiceClientOperator gitlabServiceClientOperator, UserAttrService userAttrService, PermissionHelper permissionHelper, AppServiceService appServiceService) {
        this.devopsCiPipelineMapper = devopsCiPipelineMapper;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
        this.devopsCiContentService = devopsCiContentService;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.appServiceService = appServiceService;
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO create(Long projectId, DevopsCiPipelineVO devopsCiPipelineVO) {
        Long iamUserId = TypeUtil.objToLong(GitUserNameUtil.getUserId());
        checkUserPermission(devopsCiPipelineVO.getAppServiceId(), iamUserId);
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
                devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                devopsCiJobService.create(devopsCiJobDTO);
            });
        });

        // TODO 保存ci配置文件
        saveCiContent(devopsCiPipelineDTO.getId(), devopsCiPipelineVO);

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(devopsCiPipelineDTO.getAppServiceId());
        String ciFileIncludeUrl = gatewayUrl + "/devops/v1/projects/" + projectId + "/ci_contents/pipelines/" + devopsCiPipelineDTO.getId() + "/content.yaml";
        initGitlabCiFile(appServiceDTO.getGitlabProjectId(), ciFileIncludeUrl, iamUserId);
        return devopsCiPipelineMapper.selectByPrimaryKey(devopsCiPipelineDTO.getId());
    }

    /**
     * 第一次创建CI流水线时初始化仓库下的.gitlab-ci.yml文件
     *
     * @param gitlabProjectId  gitlab项目id
     * @param ciFileIncludeUrl include中的链接
     * @param iamUserId        iam用户id
     */
    private void initGitlabCiFile(Integer gitlabProjectId, String ciFileIncludeUrl, Long iamUserId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserId);
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, GitOpsConstants.MASTER, GitOpsConstants.GITLAB_CI_FILE_NAME);

        if (repositoryFile == null) {
            // 说明项目下还没有CI文件
            // 创建文件
            gitlabServiceClientOperator.createFile(
                    gitlabProjectId,
                    GitOpsConstants.GITLAB_CI_FILE_NAME,
                    buildIncludeYaml(ciFileIncludeUrl),
                    GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        } else {
            // 将原先的配置文件内容注释并放在原本文件中
            String originFileContent = new String(Base64.getDecoder().decode(repositoryFile.getContent().getBytes()), Charset.forName("UTF-8"));
            // 注释后的内容
            String commentedLines = GitlabCiUtil.commentLines(originFileContent);
            // 更新文件
            gitlabServiceClientOperator.updateFile(
                    gitlabProjectId,
                    GitOpsConstants.GITLAB_CI_FILE_NAME,
                    buildIncludeYaml(ciFileIncludeUrl) + GitOpsConstants.NEW_LINE + commentedLines,
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
                    devopsCiJobDTO.setId(null);
                    devopsCiJobDTO.setCiStageId(devopsCiStageVO.getId());
                    devopsCiJobDTO.setCiPipelineId(ciPipelineId);
                    devopsCiJobService.create(devopsCiJobDTO);
                });
            } else {
                // 新增
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存job信息
                devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                    DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                    devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                    devopsCiJobDTO.setCiPipelineId(ciPipelineId);
                    devopsCiJobService.create(devopsCiJobDTO);
                });
            }
        });
        saveCiContent(ciPipelineId, devopsCiPipelineVO);
        return devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }

    @Override
    public DevopsCiPipelineVO query(Long projectId, Long ciPipelineId) {
        // 根据pipeline_id查询数据
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(ciPipelineId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(ciPipelineId);
        // dto转vo
        DevopsCiPipelineVO devopsCiPipelineVO = ConvertUtils.convertObject(devopsCiPipelineDTO, DevopsCiPipelineVO.class);
        List<DevopsCiStageVO> devopsCiStageVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageVO.class);
        List<DevopsCiJobVO> devopsCiJobVOS = ConvertUtils.convertList(devopsCiJobDTOS, DevopsCiJobVO.class);

        // 封装对象
        Map<Long, List<DevopsCiJobVO>> jobMap = devopsCiJobVOS.stream().collect(Collectors.groupingBy(DevopsCiJobVO::getCiStageId));
        devopsCiStageVOS.forEach(devopsCiStageVO -> {
            List<DevopsCiJobVO> ciJobVOS = jobMap.get(devopsCiStageVO.getId());
            devopsCiStageVO.setJobList(ciJobVOS);
        });
        devopsCiPipelineVO.setStageList(devopsCiStageVOS);

        return devopsCiPipelineVO;
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
        return devopsCiPipelineMapper.queryByProjectIdAndName(projectId, name);
    }

    /**
     * 校验用户是否拥有应用服务权限
     *
     * @param appServiceId 应用服务id
     * @param iamUserId    用户id
     */
    private void checkUserPermission(Long appServiceId, Long iamUserId) {
        if (!appServiceService.checkAppSerivcePermissionForUser(appServiceId, iamUserId)) {
            throw new CommonException(ERROR_USER_HAVE_NO_APP_PERMISSION);
        }

    }

    private static String buildIncludeYaml(String ciFileIncludeUrl) {
        GitlabCi gitlabCi = new GitlabCi();
        gitlabCi.setInclude(ciFileIncludeUrl);
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

    /**
     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
     *
     * @param devopsCiPipelineVO 流水线数据
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(DevopsCiPipelineVO devopsCiPipelineVO) {
        // 对阶段排序
        List<String> stages = devopsCiPipelineVO.getStageList().stream()
                .sorted(Comparator.comparing(DevopsCiStageVO::getSequence))
                .map(DevopsCiStageVO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();
        // 先使用默认的image,后面可以考虑让用户自己指定
        gitlabCi.setImage(GitOpsConstants.CI_IMAGE);
        gitlabCi.setStages(stages);
        devopsCiPipelineVO.getStageList().forEach(stageVO ->
                stageVO.getJobList().forEach(jobV0 -> {
                    CiJob ciJob = new CiJob();
                    ciJob.setStage(stageVO.getName());
                    ciJob.setOnly(buildOnlyExceptPolicyObject(jobV0.getTriggerRefs()));
                    ciJob.setScript(buildScript(jobV0));
                    // todo except && cache未配置
                    gitlabCi.addJob(jobV0.getName(), ciJob);
                })
        );
        gitlabCi.setBeforeScript(ArrayUtil.singleAsList(GitOpsConstants.CHOERODON_BEFORE_SCRIPT));
        return gitlabCi;
    }
    // todo 待处理

    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param jobVO 生成脚本
     * @return 生成的脚本列表
     */
    private List<String> buildScript(DevopsCiJobVO jobVO) {
        // TODO
        if (CiJobTypeEnum.SONAR.value().equals(jobVO.getType())) {
            // sonar配置转化为gitlab-ci配置
            List<String> scripts = new ArrayList<>();
            JSONObject jsonObject = JSON.parseObject(jobVO.getMetadata());
            SonarQubeConfigVO sonarQubeConfigVO = jsonObject.toJavaObject(SonarQubeConfigVO.class);
            if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
                throw new CommonException("error.sonar.url.is.null");
            }
            if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
                scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getUsername(), sonarQubeConfigVO.getPassword()));
            } else if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
                scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getToken()));
            }
            return scripts;
        }
        if (CiJobTypeEnum.BUILD.value().equals(jobVO.getType())) {
            // maven配置转换为gitlab-ci配置
            MavenBuildVO mavenBuildVO = JSONObject.parseObject(jobVO.getMetadata(), MavenBuildVO.class);
            if (mavenBuildVO == null || CollectionUtils.isEmpty(mavenBuildVO.getMavenbuildTemplateVOList())) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            // 将每一个step都转为一个List<String>并将所有的list合并为一个
            mavenBuildVO.getMavenbuildTemplateVOList()
                    .stream()
                    .sorted(Comparator.comparingLong(t -> TypeUtil.wrappedLongToPrimitive(t.getSequence())))
                    .map(t -> GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(t.getScript()), true, true))
                    .forEach(result::addAll);
            return result;
        }
        return Collections.emptyList();
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
