package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiSonarConfigService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiSonarConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.enums.sonar.CiSonarConfigType;
import io.choerodon.devops.infra.enums.sonar.SonarAuthType;
import io.choerodon.devops.infra.enums.sonar.SonarScannerType;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
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

    @Autowired
    private DevopsCiSonarConfigService devopsCiSonarConfigService;
    @Autowired
    private DevopsConfigService devopsConfigService;


    @Override
    protected void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        // 保存任务配置
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiStepVO.getDevopsCiSonarConfigDTO();
        devopsCiSonarConfigDTO.setStepId(stepId);
        devopsCiSonarConfigService.baseCreate(devopsCiSonarConfigDTO);
    }

    @Override
    public void fillConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        // sonar配置转化为gitlab-ci配置
        List<String> scripts = new ArrayList<>();
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiSonarConfigService.baseQuery(devopsCiStepDTO.getId());

        if (SonarScannerType.SONAR_SCANNER.value().equals(devopsCiSonarConfigDTO.getScannerType())) {
            if (CiSonarConfigType.DEFAULT.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "error.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarScannerCommand(devopsCiSonarConfigDTO.getSources()));
            } else if (CiSonarConfigType.CUSTOM.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                if (Objects.isNull(devopsCiSonarConfigDTO.getSonarUrl())) {
                    throw new CommonException("error.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommand(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getUsername(), devopsCiSonarConfigDTO.getPassword(), devopsCiSonarConfigDTO.getSources()));
                } else if (SonarAuthType.TOKEN.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommandForToken(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getToken(), devopsCiSonarConfigDTO.getSources()));
                }
            } else {
                throw new CommonException("error.sonar.config.type.not.supported", devopsCiSonarConfigDTO.getConfigType());
            }
        } else if (SonarScannerType.SONAR_MAVEN.value().equals(devopsCiSonarConfigDTO.getScannerType())) {
            if (CiSonarConfigType.DEFAULT.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "error.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarCommand(devopsCiSonarConfigDTO.getSkipTests()));
            } else if (CiSonarConfigType.CUSTOM.value().equals(devopsCiSonarConfigDTO.getConfigType())) {
                if (Objects.isNull(devopsCiSonarConfigDTO.getSonarUrl())) {
                    throw new CommonException("error.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getUsername(), devopsCiSonarConfigDTO.getPassword(), devopsCiSonarConfigDTO.getSkipTests()));
                } else if (SonarAuthType.TOKEN.value().equals(devopsCiSonarConfigDTO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommandForToken(devopsCiSonarConfigDTO.getSonarUrl(), devopsCiSonarConfigDTO.getToken(), devopsCiSonarConfigDTO.getSkipTests()));
                }
            } else {
                throw new CommonException("error.sonar.config.type.not.supported", devopsCiSonarConfigDTO.getConfigType());
            }
        } else {
            throw new CommonException(ResourceCheckConstant.ERROR_SONAR_SCANNER_TYPE_INVALID);
        }
        return scripts;
    }

    @Override
    protected void batchDeleteConfig(Set<Long> stepIds) {
        devopsCiSonarConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.SONAR;
    }
}