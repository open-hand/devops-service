package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.GitlabCiUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:33
 */
@Service
public class DevopsChartStepHandler extends AbstractDevopsCiStepHandler {

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        return ArrayUtil.singleAsList(GitlabCiUtil.generateChartBuildScripts());
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.UPLOAD_CHART;
    }
}
