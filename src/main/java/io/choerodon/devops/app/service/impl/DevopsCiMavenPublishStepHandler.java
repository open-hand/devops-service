package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:32
 */
@Component
public class DevopsCiMavenPublishStepHandler extends AbstractDevopsCiStepHandler {
    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.MAVEN_PUBLISH;

    @Override
    public void save(Long projectId, Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        super.save(projectId, devopsCiJobId, devopsCiStepVO);
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        return super.buildGitlabCiScript(devopsCiStepDTO);
    }

    @Override
    public void batchDeleteCascade(List<DevopsCiStepDTO> devopsCiStepDTOS) {
        super.batchDeleteCascade(devopsCiStepDTOS);
    }
}
