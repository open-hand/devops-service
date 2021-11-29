package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.DevopsCiSonarConfigService;
import io.choerodon.devops.app.service.DevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.infra.dto.DevopsCiSonarConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:19
 */
public class DevopsSonarStepHandler implements DevopsCiStepHandler {


    @Autowired
    private DevopsCiSonarConfigService devopsCiSonarConfigService;
    @Autowired
    private DevopsCiStepService devopsCiStepService;

    @Override
    @Transactional
    public void save(DevopsCiStepVO devopsCiStepVO) {
        // 保存任务配置
        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiStepVO.getDevopsCiSonarConfigDTO();
        devopsCiSonarConfigService.baseCreate(devopsCiSonarConfigDTO);

        // 保存步骤
        DevopsCiStepDTO devopsCiStepDTO = ConvertUtils.convertObject(devopsCiStepVO, DevopsCiStepDTO.class);
        devopsCiStepDTO.setConfigId(devopsCiSonarConfigDTO.getId());
        devopsCiStepDTO.setId(null);
        devopsCiStepService.baseCreate(devopsCiStepDTO);

    }

    @Override
    public String getType() {
        return DevopsCiStepTypeEnum.SONAR.value();
    }

}
