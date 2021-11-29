package io.choerodon.devops.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:18
 */
public abstract class AbstractDevopsCiStepHandler {
    protected DevopsCiStepTypeEnum type;

    @Autowired
    protected DevopsCiStepService devopsCiStepService;

    @Transactional
    public void save(Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        // 保存步骤
        DevopsCiStepDTO devopsCiStepDTO = ConvertUtils.convertObject(devopsCiStepVO, DevopsCiStepDTO.class);
        devopsCiStepDTO.setId(null);
        devopsCiStepDTO.setDevopsCiJobId(devopsCiJobId);
        devopsCiStepService.baseCreate(devopsCiStepDTO);
    }

    public String getType() {
        return type.value();
    }
}
