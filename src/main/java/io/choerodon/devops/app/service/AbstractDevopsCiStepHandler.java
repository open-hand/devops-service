package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import groovy.lang.Lazy;
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
    @Lazy
    protected DevopsCiStepService devopsCiStepService;

    @Transactional
    public void save(Long devopsCiJobId, DevopsCiStepVO devopsCiStepVO) {
        // 保存步骤
        DevopsCiStepDTO devopsCiStepDTO = ConvertUtils.convertObject(devopsCiStepVO, DevopsCiStepDTO.class);
        devopsCiStepDTO.setId(null);
        devopsCiStepDTO.setDevopsCiJobId(devopsCiJobId);
        devopsCiStepService.baseCreate(devopsCiStepDTO);
    }

    /**
     * 子类如果有关联的配置，则需要重写
     * @param devopsCiStepDTOS
     */
    @Transactional
    public void batchDeleteCascade(List<DevopsCiStepDTO> devopsCiStepDTOS) {
        Set<Long> ids = devopsCiStepDTOS.stream().map(DevopsCiStepDTO::getId).collect(Collectors.toSet());
        devopsCiStepService.batchDeleteByIds(ids);
    }

    public String getType() {
        return type.value();
    }
}
