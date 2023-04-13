package io.choerodon.devops.app.eventhandler.pipeline.step;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarConfigVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.Component;
import io.choerodon.devops.api.vo.sonar.SonarProjectSearchPageResult;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.service.impl.AppServiceServiceImpl;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.DevopsCiSonarQualityGateConditionMetricTypeEnum;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.enums.sonar.CiSonarConfigType;
import io.choerodon.devops.infra.enums.sonar.SonarAuthType;
import io.choerodon.devops.infra.enums.sonar.SonarScannerType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.SonarClientOperator;
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
    private DevopsCiTplSonarQualityGateConditionService devopsCiTplSonarQualityGateConditionService;
    @Autowired
    private DevopsCiTplSonarQualityGateService devopsCiTplSonarQualityGateService;
    @Autowired
    private DevopsCiMavenBuildConfigService devopsCiMavenBuildConfigService;
    @Autowired
    private DevopsCiSonarQualityGateService devopsCiSonarQualityGateService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SonarClientOperator sonarClientOperator;
    @Autowired
    private CiTemplateMavenBuildService ciTemplateMavenBuildService;


    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        DevopsCiSonarConfigVO sonarConfig = devopsCiStepVO.getSonarConfig();
        if (sonarConfig == null) {
            return;
        }
        // 报错mvn配置
        DevopsCiMavenBuildConfigVO mavenBuildConfig = sonarConfig.getMavenBuildConfig();
        if (mavenBuildConfig != null
                && (!CollectionUtils.isEmpty(mavenBuildConfig.getNexusMavenRepoIds())
                || !CollectionUtils.isEmpty(mavenBuildConfig.getRepos())
                || org.apache.commons.lang3.StringUtils.isNotBlank(mavenBuildConfig.getMavenSettings()))) {
            devopsCiMavenBuildConfigService.baseCreate(stepId, mavenBuildConfig);
        }

        // 保存任务配置
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = ConvertUtils.convertObject(sonarConfig, DevopsCiSonarConfigDTO.class);
        devopsCiSonarConfigDTO.setStepId(stepId);
        devopsCiSonarConfigDTO.setId(null);
        devopsCiSonarConfigService.baseCreate(devopsCiSonarConfigDTO);

        Long appServiceId = devopsCiStepService.queryAppServiceIdByStepId(stepId);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        String sonarProjectKey = AppServiceServiceImpl.getSonarKey(appServiceDTO.getCode(), projectDTO.getDevopsComponentCode(), organizationDTO.getTenantNum());

        // 判断sonarqube是否创建该应用，没有则创建
        checkSonarQubeProjectOrCreate(appServiceDTO.getCode(), sonarProjectKey);

        // 质量门
        if (sonarConfig.getDevopsCiSonarQualityGateVO() != null) {
            processQualityGates(sonarProjectKey, devopsCiSonarConfigDTO.getId(), sonarConfig.getDevopsCiSonarQualityGateVO());
        }
    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        // 步骤模板没有mvn配置,所以只填充maven信息
        CiTemplateSonarDTO ciTemplateSonarDTO = ciTemplateSonarService.queryByStepId(ciTemplateStepVO.getId());
        if (ciTemplateSonarDTO == null) {
            return;
        }
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = devopsCiTplSonarQualityGateService.queryBySonarConfigId(ciTemplateSonarDTO.getId());
        if (devopsCiSonarQualityGateVO != null) {
            DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateDTO = ConvertUtils.convertObject(devopsCiSonarQualityGateVO, DevopsCiTplSonarQualityGateDTO.class);
            devopsCiTplSonarQualityGateDTO.setSonarQualityGateConditionVOList(ConvertUtils.convertList(devopsCiSonarQualityGateVO.getSonarQualityGateConditionVOList(), DevopsCiTplSonarQualityGateConditionDTO.class));
            ciTemplateSonarDTO.setDevopsCiSonarQualityGateVO(devopsCiTplSonarQualityGateDTO);
        }
        ciTemplateStepVO.setSonarConfig(ciTemplateSonarDTO);
        //maven
        CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO = ciTemplateMavenBuildService.baseQueryById(ciTemplateStepVO.getId());
        if (ciTemplateMavenBuildDTO != null) {
            CiTemplateMavenBuildDTO ciTemplateMavenBuildVO = ciTemplateMavenBuildService.dtoToVo(ciTemplateMavenBuildDTO);
            ciTemplateSonarDTO.setMavenBuildConfig(ciTemplateMavenBuildVO);
            ciTemplateStepVO.setMavenBuildConfig(ciTemplateMavenBuildVO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateSonarDTO ciTemplateSonarDTO = ciTemplateStepVO.getSonarConfig();
        if (ciTemplateSonarDTO == null) {
            return;
        }
        ciTemplateSonarService.baseCreate(ciTemplateStepVO.getId(), ciTemplateSonarDTO);
    }

    @Override
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateSonarService.deleteByTemplateStepId(ciTemplateStepVO.getId());
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTemplateSonarDTO ciTemplateSonarDTO = ciTemplateSonarService.queryByStepId(devopsCiStepVO.getId());
        if (ciTemplateSonarDTO == null) {
            return;
        }
        DevopsCiSonarConfigVO devopsCiSonarConfigVO = ConvertUtils.convertObject(ciTemplateSonarDTO, DevopsCiSonarConfigVO.class);

        // 添加质量门配置
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = devopsCiTplSonarQualityGateService.queryBySonarConfigId(ciTemplateSonarDTO.getId());
        if (devopsCiSonarQualityGateVO != null) {
            devopsCiSonarConfigVO.setDevopsCiSonarQualityGateVO(devopsCiSonarQualityGateVO);
        }

        devopsCiStepVO.setSonarConfig(devopsCiSonarConfigVO);
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

        // 添加质量门配置
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = devopsCiSonarQualityGateService.queryBySonarConfigId(devopsCiSonarConfigDTO.getId());
        if (devopsCiSonarQualityGateVO != null) {
            devopsCiSonarConfigVO.setDevopsCiSonarQualityGateVO(devopsCiSonarQualityGateVO);
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

        // 有可能为null值
        Boolean blockAfterQualityGateFail = Optional.ofNullable(devopsCiSonarQualityGateService.queryBlock(devopsCiSonarConfigDTO.getId())).orElse(Boolean.FALSE);

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
                    scripts.add(GitlabCiUtil.renderSonarScannerCommand(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getUsername(), devopsCiSonarConfigDTO.getPassword(), devopsCiSonarConfigDTO.getSources(), blockAfterQualityGateFail));
                } else if (SonarAuthType.TOKEN.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommandForToken(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getToken(), devopsCiSonarConfigDTO.getSources(), blockAfterQualityGateFail));
                }
            } else {
                throw new CommonException("devops.sonar.config.type.not.supported", devopsCiSonarConfigDTO.getConfigType());
            }
        } else if (SonarScannerType.SONAR_MAVEN.value().equals(devopsCiSonarConfigDTO.getScannerType())) {
            if (devopsCiMavenBuildConfigVO != null) {
                scripts.add(0, GitlabCiUtil.downloadMavenSettings(projectId, devopsCiMavenSettingsDTO.getId()));
            }
            if (CiSonarConfigType.DEFAULT.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "devops.default.sonar.not.exist");
                if (devopsCiMavenBuildConfigVO != null) {
                    scripts.add(String.format(MVN_COMPILE_USE_SETTINGS_FUNCTION, devopsCiSonarConfigDTO.getSkipTests()));
                    scripts.add(String.format("mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -s settings.xml -Dsonar.qualitygate.wait=%s", blockAfterQualityGateFail));
                } else {
                    scripts.add(String.format(MVN_COMPILE_FUNCTION, devopsCiSonarConfigDTO.getSkipTests()));
                    scripts.add(String.format("mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.qualitygate.wait=%s", blockAfterQualityGateFail));
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

    @Override
    public Boolean isComplete(DevopsCiStepVO devopsCiStepVO) {
        if (devopsCiStepVO.getSonarConfig() == null) {
            return false;
        }
        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = devopsCiStepVO.getSonarConfig().getDevopsCiSonarQualityGateVO();
        if (devopsCiSonarQualityGateVO != null && Boolean.TRUE.equals(devopsCiSonarQualityGateVO.getGatesEnable())) {
            for (DevopsCiSonarQualityGateConditionVO devopsCiSonarQualityGateConditionVO : devopsCiSonarQualityGateVO.getSonarQualityGateConditionVOList()) {
                if (devopsCiSonarQualityGateConditionVO.getGatesMetric().equals(DevopsCiSonarQualityGateConditionMetricTypeEnum.DUPLICATED_LINES_DENSITY.getMetric()) || devopsCiSonarQualityGateConditionVO.getGatesMetric().equals(DevopsCiSonarQualityGateConditionMetricTypeEnum.NEW_DUPLICATED_LINES_DENSITY.getMetric())) {
                    if (Double.parseDouble(devopsCiSonarQualityGateConditionVO.getGatesValue()) <= 0) {
                        throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CONDITION_VALUE_SHOULD_BE_GRATER_THAN_ZERO);
                    }
                } else {
                    if (Integer.parseInt(devopsCiSonarQualityGateConditionVO.getGatesValue()) <= 0) {
                        throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CONDITION_VALUE_SHOULD_BE_GRATER_THAN_ZERO);
                    }
                }
            }
        }
        return true;
    }

    private void checkSonarQubeProjectOrCreate(String sonarProjectName, String sonarProjectKey) {

        SonarProjectSearchPageResult sonarProjectSearchPageResult = sonarClientOperator.searchProjects(sonarProjectKey);
        if (ObjectUtils.isEmpty(sonarProjectSearchPageResult.getComponents()) || sonarProjectSearchPageResult.getComponents().stream().map(Component::getKey).noneMatch(sonarProjectKey::equals)) {
            sonarClientOperator.createProject(sonarProjectName, sonarProjectKey);
        }
    }

    private void processQualityGates(String sonarProjectKey, Long configId, DevopsCiSonarQualityGateVO sonarQualityGateVO) {
        devopsCiSonarQualityGateService.deleteAll(sonarProjectKey);
        // 判断是启用还是停用质量门
        if (Boolean.TRUE.equals(sonarQualityGateVO.getGatesEnable())) {
            // 如果是启用，重新插入数据
            // 创建质量门
            devopsCiSonarQualityGateService.createGate(sonarProjectKey, configId, sonarQualityGateVO);
        }
    }
}
