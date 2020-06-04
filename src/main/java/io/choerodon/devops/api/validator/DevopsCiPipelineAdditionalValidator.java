package io.choerodon.devops.api.validator;

import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.CiJobScriptTypeEnum;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.MavenSettingsUtil;

/**
 * @author zmf
 * @since 20-4-20
 */
public class DevopsCiPipelineAdditionalValidator {
    private static final Pattern MAVEN_REPO_NAME_REGEX = Pattern.compile("[0-9a-zA-Z-]{6,30}");

    private static final String ERROR_STAGES_EMPTY = "error.stages.empty";
    private static final String ERROR_ARTIFACT_NAME_INVALID = "error.artifact.name.invalid";
    private static final String ERROR_ARTIFACT_NAME_DUPLICATED = "error.artifact.name.duplicated";
    private static final String ERROR_ARTIFACT_DOWNLOAD_BEFORE_UPLOAD = "error.artifact.download.before.upload";
    private static final String ERROR_STEP_SEQUENCE_NULl = "error.step.sequence.null";
    private static final String ERROR_STEP_SEQUENCE_DUPLICATED = "error.step.sequence.duplicated";
    private static final String ERROR_MAVEN_REPO_TYPE_EMPTY = "error.maven.repository.type.null";
    private static final String ERROR_MAVEN_REPO_TYPE_INVALID = "error.maven.repository.type.invalid";
    private static final String ERROR_MAVEN_REPO_NAME_EMPTY = "error.maven.repository.name.empty";
    private static final String ERROR_MAVEN_REPO_NAME_INVALID = "error.maven.repository.name.invalid";
    private static final String ERROR_MAVEN_REPO_URL_EMPTY = "error.maven.repository.url.empty";
    private static final String ERROR_MAVEN_REPO_URL_INVALID = "error.maven.repository.url.invalid";
    private static final String ERROR_MAVEN_REPO_USERNAME_EMPTY = "error.maven.repository.username.empty";
    private static final String ERROR_MAVEN_REPO_PASSWORD_EMPTY = "error.maven.repository.password.empty";
    private static final String ERROR_CUSTOM_JOB_FORMAT_INVALID = "error.custom.job.format.invalid";
    private static final String ERROR_CUSTOM_JOB_STAGE_NOT_MATCH = "error.custom.job.stage.not.match";
    private static final String ERROR_JOB_NAME_NOT_UNIQUE = "error.job.name.not.unique";
    private static final String ERROR_STAGE_NAME_NOT_UNIQUE = "error.stage.name.not.unique";
    private static final String ERROR_BOTH_REPOS_AND_SETTINGS_EXIST = "error.both.repos.and.settings.exist";
    private static final String ERROR_MAVEN_SETTINGS_NOT_XML_FORMAT = "error.maven.settings.not.xml.format";

    private DevopsCiPipelineAdditionalValidator() {
    }

    /**
     * 是对JSR303无法校验的部分进行补充性的校验
     *
     * @param devopsCiPipelineVO 流水线数据
     */
    public static void additionalCheckPipeline(DevopsCiPipelineVO devopsCiPipelineVO) {
        if (CollectionUtils.isEmpty(devopsCiPipelineVO.getStageList())) {
            throw new CommonException(ERROR_STAGES_EMPTY);
        }

        List<String> uploadArtifactNames = new ArrayList<>();
        List<String> jobNames = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();

        validateImage(devopsCiPipelineVO.getImage());

        devopsCiPipelineVO.getStageList()
                .stream()
                .sorted(Comparator.comparingLong(DevopsCiStageVO::getSequence))
                .forEach(stage -> {
                    if (CollectionUtils.isEmpty(stage.getJobList())) {
                        return;
                    }

                    // 校验stage名称唯一
                    validateStageNameUniqueInPipeline(stage.getName(), stageNames);

                    stage.getJobList().forEach(job -> {
                        validateImage(job.getImage());
                        validateCustomJobFormat(Objects.requireNonNull(stage.getName()), job);
                        validateJobNameUniqueInPipeline(job.getName(), jobNames);

                        if (CiJobTypeEnum.BUILD.value().equals(job.getType())) {
                            // 将构建类型的stage中的job的每个step进行解析和转化
                            CiConfigVO ciConfigVO = JSONObject.parseObject(job.getMetadata(), CiConfigVO.class);
                            if (ciConfigVO == null || CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                                return;
                            }
                            ciConfigVO.getConfig()
                                    .stream()
                                    .sorted(Comparator.comparingLong(CiConfigTemplateVO::getSequence))
                                    .forEach(config -> {
                                        if (CiJobScriptTypeEnum.UPLOAD.getType().equals(config.getType())) {
                                            if (config.getArtifactFileName() == null || !GitOpsConstants.ARTIFACT_NAME_PATTERN.matcher(config.getArtifactFileName()).matches()) {
                                                throw new CommonException(ERROR_ARTIFACT_NAME_INVALID, config.getArtifactFileName());
                                            }
                                            if (uploadArtifactNames.contains(config.getArtifactFileName())) {
                                                throw new CommonException(ERROR_ARTIFACT_NAME_DUPLICATED, config.getArtifactFileName(), config.getName());
                                            }
                                            uploadArtifactNames.add(config.getArtifactFileName());
                                        } else if (CiJobScriptTypeEnum.DOCKER.getType().equals(config.getType())) {
                                            if (config.getArtifactFileName() != null) {
                                                if (!GitOpsConstants.ARTIFACT_NAME_PATTERN.matcher(config.getArtifactFileName()).matches()) {
                                                    throw new CommonException(ERROR_ARTIFACT_NAME_INVALID, config.getArtifactFileName());
                                                }
                                                // 进行这个判断的前提是stage和step是有序的。
                                                // 如果在此之前，没有在上传的软件包列表中没有找到同名的，那就是在上传软件包之前就使用了。
                                                if (!uploadArtifactNames.contains(config.getArtifactFileName())) {
                                                    throw new CommonException(ERROR_ARTIFACT_DOWNLOAD_BEFORE_UPLOAD, config.getArtifactFileName(), config.getName());
                                                }
                                            }
                                        }
                                    });
                        }
                    });
                });
    }

    /**
     * 校验sequence不为null也不重复
     *
     * @param sequence         step的序列号
     * @param templateName     构建步骤的名称，用于报错信息
     * @param existedSequences 已经存在的sequence
     */
    public static void validConfigSequence(@Nullable Long sequence, String templateName, List<Long> existedSequences) {
        if (sequence == null) {
            throw new CommonException(ERROR_STEP_SEQUENCE_NULl, templateName);
        }
        if (existedSequences.contains(sequence)) {
            throw new CommonException(ERROR_STEP_SEQUENCE_DUPLICATED, templateName);
        }
        existedSequences.add(sequence);
    }

    /**
     * 校验maven步骤的参数
     *
     * @param config maven步骤数据
     */
    public static void validateMavenStep(CiConfigTemplateVO config) {
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
                        throw new CommonException(ERROR_MAVEN_REPO_PASSWORD_EMPTY);
                    }
                }
            });

            // 两个字段只能填一个
            if (!StringUtils.isEmpty(config.getMavenSettings())) {
                throw new CommonException(ERROR_BOTH_REPOS_AND_SETTINGS_EXIST, config.getName());
            }
        }

        // 校验用户直接粘贴的maven的settings文件的内容
        if (!StringUtils.isEmpty(config.getMavenSettings())) {
            // 如果不符合xml格式，抛异常
            if (!MavenSettingsUtil.isXmlFormat(config.getMavenSettings())) {
                throw new CommonException(ERROR_MAVEN_SETTINGS_NOT_XML_FORMAT, config.getName());
            }
        }
    }

    /**
     * 校验runner的镜像地址是否正确
     *
     * @param image 镜像地址
     */
    private static void validateImage(String image) {
        if (image == null) {
            return;
        }
        if (!GitOpsConstants.IMAGE_REGISTRY.matcher(image).matches()) {
            throw new CommonException("error.ci.image.invalid", image);
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
        Object load = yaml.load(devopsCiJobVO.getMetadata());
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
            JSONObject jsonObject = new JSONObject((Map<String, Object>) value);
            String stageNameDefinedInJob = jsonObject.getString(GitOpsConstants.STAGE);
            CommonExAssertUtil.assertTrue(stageName.equals(stageNameDefinedInJob), ERROR_CUSTOM_JOB_STAGE_NOT_MATCH, stageNameDefinedInJob, stageName);
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
