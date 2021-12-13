package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiSonarConfigService;
import io.choerodon.devops.infra.dto.DevopsCiSonarConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsCiSonarConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:26
 */
@Service
public class DevopsCiSonarConfigServiceImpl implements DevopsCiSonarConfigService {
    @Autowired
    private DevopsCiSonarConfigMapper devopsCiSonarConfigMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsCiSonarConfigDTO devopsCiSonarConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiSonarConfigMapper,
                devopsCiSonarConfigDTO,
                "error.save.ci.sonar.config.failed");
    }

    @Override
    @Transactional
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        devopsCiSonarConfigMapper.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiSonarConfigDTO baseQuery(Long id) {
        return devopsCiSonarConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiSonarConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, "error.step.id.is.null");

        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = new DevopsCiSonarConfigDTO();
        devopsCiSonarConfigDTO.setStepId(stepId);

        return devopsCiSonarConfigMapper.selectOne(devopsCiSonarConfigDTO);
    }
}
