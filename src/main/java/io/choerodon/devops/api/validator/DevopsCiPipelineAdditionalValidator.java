package io.choerodon.devops.api.validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;

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
    private static final String ERROR_STAGES_EMPTY = "error.stages.empty";
    private static final String ERROR_ARTIFACT_NAME_INVALID = "error.artifact.name.invalid";
    private static final String ERROR_ARTIFACT_NAME_DUPLICATED = "error.artifact.name.duplicated";
    private static final String ERROR_ARTIFACT_DOWNLOAD_BEFORE_UPLOAD = "error.artifact.download.before.upload";

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
}
