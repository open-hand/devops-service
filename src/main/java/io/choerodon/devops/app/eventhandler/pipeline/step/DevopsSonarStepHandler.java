package io.choerodon.devops.app.eventhandler.pipeline.step;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarConfigVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateSonarService;
import io.choerodon.devops.app.service.DevopsCiMavenBuildConfigService;
import io.choerodon.devops.app.service.DevopsCiSonarConfigService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.enums.sonar.CiSonarConfigType;
import io.choerodon.devops.infra.enums.sonar.SonarAuthType;
import io.choerodon.devops.infra.enums.sonar.SonarScannerType;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitlabCiUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:19
 */
@Service
public class DevopsSonarStepHandler extends AbstractDevopsCiStepHandler {

    private static final String SAVE_SONAR_INFO_FUNCTION = "saveSonarInfo %s";
    private static final String MVN_COMPILE_FUNCTION = "mvnCompile %s";
    private static final String MVN_COMPILE_USE_SETTINGS_FUNCTION = "mvnCompileUseSettings %s";

    @Autowired
    private DevopsCiSonarConfigService devopsCiSonarConfigService;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private CiTemplateSonarService ciTemplateSonarService;
    @Autowired
    private DevopsCiMavenBuildConfigService devopsCiMavenBuildConfigService;

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        DevopsCiSonarConfigVO sonarConfig = devopsCiStepVO.getSonarConfig();
        // 报错mvn配置
        DevopsCiMavenBuildConfigVO mavenBuildConfig = sonarConfig.getMavenBuildConfig();
        if (mavenBuildConfig != null
                && (!CollectionUtils.isEmpty(mavenBuildConfig.getNexusMavenRepoIds()) || !CollectionUtils.isEmpty(mavenBuildConfig.getRepos()))) {
            devopsCiMavenBuildConfigService.baseCreate(stepId, mavenBuildConfig);
        }

        // 保存任务配置
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = ConvertUtils.convertObject(sonarConfig, DevopsCiSonarConfigDTO.class);
        devopsCiSonarConfigDTO.setStepId(stepId);
        devopsCiSonarConfigDTO.setId(null);
        devopsCiSonarConfigService.baseCreate(devopsCiSonarConfigDTO);

    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        // 步骤模板没有mvn配置,所以只填充maven信息
        ciTemplateStepVO.setSonarConfig(ciTemplateSonarService.queryByStepId(ciTemplateStepVO.getId()));
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTemplateSonarDTO ciTemplateSonarDTO = ciTemplateSonarService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setSonarConfig(ConvertUtils.convertObject(ciTemplateSonarDTO, DevopsCiSonarConfigVO.class));
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiSonarConfigService.queryByStepId(devopsCiStepVO.getId());
        DevopsCiSonarConfigVO devopsCiSonarConfigVO = ConvertUtils.convertObject(devopsCiSonarConfigDTO, DevopsCiSonarConfigVO.class);

        // 添加maven配置
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = devopsCiMavenBuildConfigService.queryByStepId(devopsCiStepVO.getId());
        if (devopsCiMavenBuildConfigDTO != null) {
            devopsCiSonarConfigVO.setMavenBuildConfig(devopsCiMavenBuildConfigService.dtoToVo(devopsCiMavenBuildConfigDTO));
        }

        devopsCiStepVO.setSonarConfig(devopsCiSonarConfigVO);
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();
        // 处理settings文件
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = devopsCiMavenBuildConfigService.queryUnmarshalByStepId(devopsCiStepDTO.getId());
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = null;
        if (devopsCiMavenBuildConfigVO != null) {
            DevopsCiPipelineAdditionalValidator.validateMavenBuildStep(devopsCiMavenBuildConfigVO);
            devopsCiMavenSettingsDTO = devopsCiMavenBuildConfigService.buildAndSaveMavenSettings(projectId,
                    devopsCiJobId,
                    devopsCiStepDTO.getSequence(),
                    devopsCiMavenBuildConfigVO);
        }

        // sonar配置转化为gitlab-ci配置
        List<String> scripts = new ArrayList<>();
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiSonarConfigService.queryByStepId(devopsCiStepDTO.getId());

        if (SonarScannerType.SONAR_SCANNER.value().equals(devopsCiSonarConfigDTO.getScannerType())) {
            if (CiSonarConfigType.DEFAULT.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "devops.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarScannerCommand(devopsCiSonarConfigDTO.getSources()));
            } else if (CiSonarConfigType.CUSTOM.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                if (Objects.isNull(devopsCiSonarConfigDTO.getSonarUrl())) {
                    throw new CommonException("devops.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommand(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getUsername(), devopsCiSonarConfigDTO.getPassword(), devopsCiSonarConfigDTO.getSources()));
                } else if (SonarAuthType.TOKEN.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommandForToken(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getToken(), devopsCiSonarConfigDTO.getSources()));
                }
            } else {
                throw new CommonException("devops.sonar.config.type.not.supported", devopsCiSonarConfigDTO.getConfigType());
            }
        } else if (SonarScannerType.SONAR_MAVEN.value().equals(devopsCiSonarConfigDTO.getScannerType())) {
            DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = devopsCiMavenBuildConfigService.queryByStepId(devopsCiStepDTO.getId());
            boolean hasSettingConfig = devopsCiMavenBuildConfigDTO != null;
            if (hasSettingConfig) {
                scripts.add(0, GitlabCiUtil.downloadMavenSettings(projectId, devopsCiMavenSettingsDTO.getId()));
            }
            if (CiSonarConfigType.DEFAULT.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "devops.default.sonar.not.exist");
                if (hasSettingConfig) {
                    scripts.add(String.format(MVN_COMPILE_USE_SETTINGS_FUNCTION, devopsCiSonarConfigDTO.getSkipTests()));
                    scripts.add("mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.gitlab.project_id=$CI_PROJECT_PATH -Dsonar.gitlab.commit_sha=$CI_COMMIT_REF_NAME -Dsonar.gitlab.ref_name=$CI_COMMIT_REF_NAME -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -s settings.xml");
                } else {
                    scripts.add(String.format(MVN_COMPILE_FUNCTION, devopsCiSonarConfigDTO.getSkipTests()));
                    scripts.add("mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.gitlab.project_id=$CI_PROJECT_PATH -Dsonar.gitlab.commit_sha=$CI_COMMIT_REF_NAME -Dsonar.gitlab.ref_name=$CI_COMMIT_REF_NAME -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY}");
                }

            } else if (CiSonarConfigType.CUSTOM.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                if (Objects.isNull(devopsCiSonarConfigDTO.getSonarUrl())) {
                    throw new CommonException("devops.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getUsername(), devopsCiSonarConfigDTO.getPassword(), devopsCiSonarConfigDTO.getSkipTests()));
                } else if (SonarAuthType.TOKEN.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommandForToken(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getToken(), devopsCiSonarConfigDTO.getSkipTests()));
                }
            } else {
                throw new CommonException("devops.sonar.config.type.not.supported", devopsCiSonarConfigDTO.getConfigType());
            }
        } else {
            throw new CommonException(ResourceCheckConstant.DEVOPS_SONAR_SCANNER_TYPE_INVALID);
        }
        scripts.add(String.format(SAVE_SONAR_INFO_FUNCTION, devopsCiSonarConfigDTO.getScannerType()));
        return scripts;
    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
        devopsCiMavenBuildConfigService.batchDeleteByStepIds(stepIds);
        devopsCiSonarConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.SONAR;
    }

    @Override
    protected Boolean isConfigComplete(DevopsCiStepVO ciTemplateStepVO) {
        DevopsCiSonarConfigVO sonarConfig = ciTemplateStepVO.getSonarConfig();
        if (sonarConfig == null) {
            return false;
        }
        if (sonarConfig.getScannerType() == null) {
            return false;
        }
        if (SonarScannerType.SONAR_MAVEN.value().equals(sonarConfig.getScannerType())) {
            if (sonarConfig.getSkipTests() == null) {
                return false;
            }
        } else if (SonarScannerType.SONAR_SCANNER.value().equals(sonarConfig.getScannerType())) {
            if (!StringUtils.hasText(sonarConfig.getSources())) {
                return false;
            }
        }
        if (!StringUtils.hasText(sonarConfig.getConfigType())) {
            return false;
        }
        if (CiSonarConfigType.DEFAULT.value().equals(sonarConfig.getConfigType())) {
            return true;
        } else if (CiSonarConfigType.CUSTOM.value().equals(sonarConfig.getConfigType())) {
            if (!StringUtils.hasText(sonarConfig.getSonarUrl())) {
                return false;
            }
            if (!StringUtils.hasText(sonarConfig.getAuthType())) {
                return false;
            }
            if (SonarAuthType.USERNAME_PWD.value().equals(sonarConfig.getAuthType())) {
                if (!StringUtils.hasText(sonarConfig.getUsername())) {
                    return false;
                }
                return StringUtils.hasText(sonarConfig.getPassword());
            } else if (SonarAuthType.TOKEN.value().equals(sonarConfig.getAuthType())) {
                return StringUtils.hasText(sonarConfig.getToken());
            }

        }
        return true;
    }
}
