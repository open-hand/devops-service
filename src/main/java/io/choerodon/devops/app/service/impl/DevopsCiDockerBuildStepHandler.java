package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;
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
    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.DOCKER_BUILD;
    @Autowired
    private DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService;

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
        // TODO 修复 目前后端这个参数的含义是是否跳过证书校验, 前端的含义是是否进行证书校验
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiDockerBuildConfigService.queryByStepId(devopsCiStepDTO.getId());

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

    @Override
    @Transactional
    public void save(Long projectId, Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        // 保存步骤
        DevopsCiStepDTO devopsCiStepDTO = ConvertUtils.convertObject(devopsCiStepVO, DevopsCiStepDTO.class);
        devopsCiStepDTO.setId(null);
        devopsCiStepDTO.setProjectId(projectId);
        devopsCiStepDTO.setDevopsCiJobId(devopsCiJobId);
        devopsCiStepService.baseCreate(devopsCiStepDTO);

        // 保存任务配置
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiStepVO.getDevopsCiDockerBuildConfigDTO();
        devopsCiDockerBuildConfigDTO.setStepId(devopsCiStepDTO.getId());
        devopsCiDockerBuildConfigService.baseCreate(devopsCiDockerBuildConfigDTO);
    }

    @Override
    public void batchDeleteCascade(List<DevopsCiStepDTO> devopsCiStepDTOS) {
        super.batchDeleteCascade(devopsCiStepDTOS);

        // 删除关联的配置
        Set<Long> stepIds = devopsCiStepDTOS.stream().map(DevopsCiStepDTO::getId).collect(Collectors.toSet());
        devopsCiDockerBuildConfigService.batchDeleteByStepIds(stepIds);
    }
    @Override
    public String getType() {
        return type.value();
    }
}
