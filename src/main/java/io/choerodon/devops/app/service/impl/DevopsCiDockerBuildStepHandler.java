package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;
import io.choerodon.devops.infra.dto.CiTemplateDockerDTO;
import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitlabCiUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:35
 */
@Service
public class DevopsCiDockerBuildStepHandler extends AbstractDevopsCiStepHandler {
    @Autowired
    private DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService;
    @Autowired
    private CiTemplateDockerService ciTemplateDockerService;

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateStepVO.setDockerBuildConfig(ciTemplateDockerService.queryByStepId(ciTemplateStepVO.getId()));
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTemplateDockerDTO ciTemplateDockerDTO = ciTemplateDockerService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setDockerBuildConfig(ConvertUtils.convertObject(ciTemplateDockerDTO, DevopsCiDockerBuildConfigDTO.class));
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiDockerBuildConfigService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setDockerBuildConfig(devopsCiDockerBuildConfigDTO);
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
        // TODO 修复 目前后端这个参数的含义是是否跳过证书校验, 前端的含义是是否进行证书校验
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiDockerBuildConfigService.queryByStepId(devopsCiStepDTO.getId());

        Boolean doTlsVerify = devopsCiDockerBuildConfigDTO.getEnableDockerTlsVerify();
        //是否开启镜像扫描 默认是关闭镜像扫描的
        Boolean imageScan = devopsCiDockerBuildConfigDTO.getImageScan();
        return GitlabCiUtil.generateDockerScripts(
                devopsCiDockerBuildConfigDTO.getDockerContextDir(),
                devopsCiDockerBuildConfigDTO.getDockerFilePath(),
                doTlsVerify == null || !doTlsVerify,
                !Objects.isNull(imageScan) && imageScan,
                devopsCiStepDTO.getDevopsCiJobId());
    }

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        // 保存任务配置
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiStepVO.getDockerBuildConfig();
        devopsCiDockerBuildConfigDTO.setStepId(stepId);
        devopsCiDockerBuildConfigService.baseCreate(devopsCiDockerBuildConfigDTO);
    }

    @Override
    @Transactional
    public void batchDeleteConfig(Set<Long> stepIds) {
        devopsCiDockerBuildConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.DOCKER_BUILD;
    }

    @Override
    protected Boolean isConfigComplete(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateDockerDTO dockerBuildConfig = ciTemplateStepVO.getDockerBuildConfig();
        if (dockerBuildConfig == null) {
            return false;
        }
        if (!StringUtils.hasText(dockerBuildConfig.getDockerContextDir())) {
            return false;
        }
        if (!StringUtils.hasText(dockerBuildConfig.getDockerFilePath())) {
            return false;
        }
        if (dockerBuildConfig.getEnableDockerTlsVerify() == null) {
            return false;
        }
        if (dockerBuildConfig.getImageScan() == null) {
            return false;
        }
        if (Boolean.FALSE.equals(dockerBuildConfig.getImageScan())) {
            return true;
        }
        if (dockerBuildConfig.getSecurityControl() == null) {
            return false;
        }
        if (Boolean.FALSE.equals(dockerBuildConfig.getSecurityControl())) {
            return true;
        }

        if (!StringUtils.hasText(dockerBuildConfig.getSeverity())) {
            return false;
        }
        if (!StringUtils.hasText(dockerBuildConfig.getSecurityControlConditions())) {
            return false;
        }
        if (dockerBuildConfig.getVulnerabilityCount() == null) {
            return false;
        }

        return true;
    }
}
