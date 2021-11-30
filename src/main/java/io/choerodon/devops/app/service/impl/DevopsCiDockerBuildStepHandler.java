package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;
import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
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
    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.DOCKER_BUILD;
    @Autowired
    private DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService;

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
        // TODO 修复 目前后端这个参数的含义是是否跳过证书校验, 前端的含义是是否进行证书校验
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiDockerBuildConfigService.baseQuery(devopsCiStepDTO.getConfigId());

        Boolean doTlsVerify = devopsCiDockerBuildConfigDTO.getSkipDockerTlsVerify();
        //是否开启镜像扫描 默认是关闭镜像扫描的
        Boolean imageScan = devopsCiDockerBuildConfigDTO.getImageScan();
        return GitlabCiUtil.generateDockerScripts(
                devopsCiDockerBuildConfigDTO.getDockerContextDir(),
                devopsCiDockerBuildConfigDTO.getDockerFilePath(),
                doTlsVerify == null || !doTlsVerify,
                !Objects.isNull(imageScan) && imageScan,
                devopsCiStepDTO.getDevopsCiJobId());
    }


}
