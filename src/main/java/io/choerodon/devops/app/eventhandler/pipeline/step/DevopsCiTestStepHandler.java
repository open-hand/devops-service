package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

@Component
public class DevopsCiTestStepHandler extends AbstractDevopsCiStepHandler {
    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.API_TEST;
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
//        Long projectId = devopsCiStepDTO.getProjectId();
//        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();
//
//        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = devopsCiMavenPublishConfigService.queryByStepId(devopsCiStepDTO.getId());
//        DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = dtoToVo(devopsCiMavenPublishConfigDTO);
//
//        List<MavenRepoVO> targetRepos = new ArrayList<>();
//        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = buildAndSaveJarDeployMavenSettings(projectId,
//                devopsCiJobId,
//                devopsCiMavenPublishConfigVO,
//                devopsCiStepDTO,
//                targetRepos);
//        return buildMavenJarDeployScripts(projectId,
//                devopsCiJobId,
//                devopsCiMavenSettingsDTO,
//                devopsCiMavenPublishConfigVO,
//                devopsCiStepDTO,
//                targetRepos);
        return null;
    }
}
