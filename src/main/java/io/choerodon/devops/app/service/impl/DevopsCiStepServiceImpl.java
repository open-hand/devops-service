package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.mapper.DevopsCiStepMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:22
 */
@Service
public class DevopsCiStepServiceImpl implements DevopsCiStepService {
    @Autowired
    private DevopsCiStepMapper devopsCiStepMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsCiStepDTO devopsCiStepDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiStepMapper,
                devopsCiStepDTO,
                "error.save.ci.step.failed");
    }
}
