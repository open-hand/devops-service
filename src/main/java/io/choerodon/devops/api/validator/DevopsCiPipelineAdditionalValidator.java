package io.choerodon.devops.api.validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiConfigTemplateVO;
import io.choerodon.devops.api.vo.CiConfigVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.api.vo.DevopsCiStageVO;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.CiJobScriptTypeEnum;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;

/**
 * @author zmf
 * @since 20-4-20
 */
public class DevopsCiPipelineAdditionalValidator {
    private static final Pattern MAVEN_REPO_NAME_REGEX = Pattern.compile("[0-9a-zA-Z]{6,30}");

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

        devopsCiPipelineVO.getStageList()
                .stream()
                .sorted(Comparator.comparingLong(DevopsCiStageVO::getSequence))
                .forEach(stage -> {
                    if (CollectionUtils.isEmpty(stage.getJobList())) {
                        return;
                    }
                    stage.getJobList().forEach(job -> {
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
        }
    }
}
