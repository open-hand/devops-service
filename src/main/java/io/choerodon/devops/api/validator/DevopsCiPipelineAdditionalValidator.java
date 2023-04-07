package io.choerodon.devops.api.validator;

import java.util.*;
import java.util.regex.Pattern;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.util.MavenSettingsUtil;

/**
 * @author zmf
 * @since 20-4-20
 */
public class DevopsCiPipelineAdditionalValidator {
    private static final Pattern MAVEN_REPO_NAME_REGEX = Pattern.compile("[0-9a-zA-Z-]{6,30}");
    private static final Pattern PIPELINE_VARIABLE_KEY_FORMAT = Pattern.compile("^\\w+$");

    private static final String ERROR_STAGES_EMPTY = "devops.stages.empty";
    private static final String ERROR_CI_JOB_IS_EMPTY = "devops.ci.job.is.empty";
    private static final String ERROR_PIPELINE_RELATED_BRANCH_EMPTY = "devops.pipeline.related.branch.empty";
    private static final String ERROR_JOB_PARALLEL_SIZE_RANGE = "devops.job.parallel.size.range";
    private static final String ERROR_PIPELINE_RELATED_BRANCH_SIZE_INVALID = "devops.pipeline.related.branch.size.invalid";
    private static final String ERROR_MAVEN_REPO_TYPE_EMPTY = "devops.maven.repository.type.null";
    private static final String ERROR_MAVEN_REPO_TYPE_INVALID = "devops.maven.repository.type.invalid";
    private static final String ERROR_MAVEN_REPO_NAME_EMPTY = "devops.maven.repository.name.empty";
    private static final String ERROR_MAVEN_REPO_NAME_INVALID = "devops.maven.repository.name.invalid";
    private static final String ERROR_MAVEN_REPO_URL_EMPTY = "devops.maven.repository.url.empty";
    private static final String ERROR_MAVEN_REPO_URL_INVALID = "devops.maven.repository.url.invalid";
    private static final String ERROR_MAVEN_REPO_USERNAME_EMPTY = "devops.maven.repository.username.empty";
    private static final String ERROR_MAVEN_REPO_PSW_EMPTY = "devops.maven.repository.password.empty";
    private static final String ERROR_CUSTOM_JOB_FORMAT_INVALID = "devops.custom.job.format.invalid";
    private static final String ERROR_JOB_NAME_NOT_UNIQUE = "devops.job.name.not.unique";
    private static final String ERROR_STAGE_NAME_NOT_UNIQUE = "devops.stage.name.not.unique";
    private static final String ERROR_BOTH_REPOS_AND_SETTINGS_EXIST = "devops.both.repos.and.settings.exist";
    private static final String ERROR_MAVEN_SETTINGS_NOT_XML_FORMAT = "devops.maven.settings.not.xml.format";

    private DevopsCiPipelineAdditionalValidator() {
    }

    /**
     * 是对JSR303无法校验的部分进行补充性的校验
     *
     * @param ciCdPipelineVO 流水线数据
     */
    public static void additionalCheckPipeline(CiCdPipelineVO ciCdPipelineVO) {
        if (CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            throw new CommonException(ERROR_STAGES_EMPTY);
        }

        List<String> jobNames = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();

        ciCdPipelineVO.getDevopsCiStageVOS()
                .stream()
                .sorted(Comparator.comparingLong(DevopsCiStageVO::getSequence))
                .forEach(stage -> {
                    if (CollectionUtils.isEmpty(stage.getJobList())) {
                        throw new CommonException(ERROR_CI_JOB_IS_EMPTY, stage.getName());
                    }

                    // 校验stage名称唯一
                    validateStageNameUniqueInPipeline(stage.getName(), stageNames);

                    stage.getJobList().forEach(job -> {
                        validateParallel(job);
                        validateCustomJobFormat(Objects.requireNonNull(stage.getName()), job);
                        validateJobNameUniqueInPipeline(job.getName(), jobNames);
                        validateJobStep(job);

                    });
                });

    }

    private static void validateJobStep(DevopsCiJobVO job) {
        if (CiJobTypeEnum.NORMAL.value().equals(job.getType())) {
            List<DevopsCiStepVO> devopsCiStepVOList = job.getDevopsCiStepVOList();
            if (CollectionUtils.isEmpty(devopsCiStepVOList)) {
                throw new CommonException("devops.job.step.is.empty", job.getName());
            }
        }
    }

    /**
     * 校验流水线执行变量的key格式
     *
     * @param variables
     */
    public static void additionalCheckVariablesKey(Map<String, String> variables) {
        variables.forEach((k, v) -> {
            if (!PIPELINE_VARIABLE_KEY_FORMAT.matcher(k).matches()) {
                throw new CommonException("devops.variable.key.format.invalid");
            }
        });
    }

    private static void validateParallel(DevopsCiJobVO job) {
        if (job.getParallel() != null
                && (job.getParallel() < 2 || job.getParallel() > 50)) {
            throw new CommonException(ERROR_JOB_PARALLEL_SIZE_RANGE);
        }
    }

    public static void validateBranch(Set<String> relatedBranches) {
        if (CollectionUtils.isEmpty(relatedBranches)) {
            throw new CommonException(ERROR_PIPELINE_RELATED_BRANCH_EMPTY);
        }
        if (relatedBranches.size() > 5) {
            throw new CommonException(ERROR_PIPELINE_RELATED_BRANCH_SIZE_INVALID);
        }
    }


    /**
     * 校验maven步骤的参数
     *
     * @param config maven步骤数据
     */
    public static void validateMavenBuildStep(DevopsCiMavenBuildConfigVO config) {
        // 主要是校验仓库设置
        if (!CollectionUtils.isEmpty(config.getRepos())) {
            config.getRepos().forEach(repo -> {
                if (StringUtils.isEmpty(repo.getType())) {
                    throw new CommonException(ERROR_MAVEN_REPO_TYPE_EMPTY);
                }
                String[] types = repo.getType().split(GitOpsConstants.COMMA);
                if (types.length > 2) {
                    throw new CommonException(ERROR_MAVEN_REPO_TYPE_INVALID, repo.getType());
                }

                if (StringUtils.isEmpty(repo.getName())) {
                    throw new CommonException(ERROR_MAVEN_REPO_NAME_EMPTY);
                }

                if (!MAVEN_REPO_NAME_REGEX.matcher(repo.getName()).matches()) {
                    throw new CommonException(ERROR_MAVEN_REPO_NAME_INVALID, repo.getName());
                }

                if (StringUtils.isEmpty(repo.getUrl())) {
                    throw new CommonException(ERROR_MAVEN_REPO_URL_EMPTY);
                }

                if (!GitOpsConstants.HTTP_URL_PATTERN.matcher(repo.getUrl()).matches()) {
                    throw new CommonException(ERROR_MAVEN_REPO_URL_INVALID, repo.getUrl());
                }

                if (Boolean.TRUE.equals(repo.getPrivateRepo())) {
                    if (StringUtils.isEmpty(repo.getUsername())) {
                        throw new CommonException(ERROR_MAVEN_REPO_USERNAME_EMPTY);
                    }
                    if (StringUtils.isEmpty(repo.getPassword())) {
                        throw new CommonException(ERROR_MAVEN_REPO_PSW_EMPTY);
                    }
                }
            });

            // 两个字段只能填一个
            if (!StringUtils.isEmpty(config.getMavenSettings())) {
                throw new CommonException(ERROR_BOTH_REPOS_AND_SETTINGS_EXIST);
            }
        }

        // 校验用户直接粘贴的maven的settings文件的内容
        if (!StringUtils.isEmpty(config.getMavenSettings())
                && !MavenSettingsUtil.isXmlFormat(config.getMavenSettings())) {
            // 如果不符合xml格式，抛异常
            throw new CommonException(ERROR_MAVEN_SETTINGS_NOT_XML_FORMAT);
        }
    }


    /**
     * 校验自定义任务格式
     */
    @SuppressWarnings("unchecked")
    private static void validateCustomJobFormat(String stageName, DevopsCiJobVO devopsCiJobVO) {
        if (!CiJobTypeEnum.CUSTOM.value().equalsIgnoreCase(devopsCiJobVO.getType())) {
            return;
        }
        Yaml yaml = new Yaml();
        Object load;
        try {
            load = yaml.load(devopsCiJobVO.getScript());
        } catch (Exception ex) {
            throw new CommonException(ERROR_CUSTOM_JOB_FORMAT_INVALID);
        }

        // 不是yaml格式报错
        if (!(load instanceof Map)) {
            throw new CommonException(ERROR_CUSTOM_JOB_FORMAT_INVALID);
        }

        // 校验自定义yaml的 job name和stage name 是否匹配
        ((Map<String, Object>) load).forEach((key, value) -> {
            if (org.apache.commons.lang3.StringUtils.isBlank(key)) {
                throw new CommonException(ERROR_CUSTOM_JOB_FORMAT_INVALID);
            }
            devopsCiJobVO.setName(key);
        });
    }

    /**
     * 校验job的name唯一，校验通过会将jobName加入list
     *
     * @param jobName  job的name
     * @param jobNames 已有的job name
     */
    private static void validateJobNameUniqueInPipeline(String jobName, List<String> jobNames) {
        if (jobNames.contains(jobName)) {
            throw new CommonException(ERROR_JOB_NAME_NOT_UNIQUE, jobName);
        }
        jobNames.add(jobName);
    }

    /**
     * 校验stage的name唯一，校验通过会将stageName加入list
     *
     * @param stageName  stage的name
     * @param stageNames 已有的stage name
     */
    private static void validateStageNameUniqueInPipeline(String stageName, List<String> stageNames) {
        if (stageNames.contains(stageName)) {
            throw new CommonException(ERROR_STAGE_NAME_NOT_UNIQUE);
        }
        stageNames.add(stageName);
    }
}
