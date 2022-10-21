package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL;

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

    private static final String DEVOPS_SAVE_CI_SONAR_CONFIG_FAILED = "devops.save.ci.sonar.config.failed";

    @Autowired
    private DevopsCiSonarConfigMapper devopsCiSonarConfigMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsCiSonarConfigDTO devopsCiSonarConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiSonarConfigMapper,
                devopsCiSonarConfigDTO,
                DEVOPS_SAVE_CI_SONAR_CONFIG_FAILED);
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
        Assert.notNull(stepId, DEVOPS_STEP_ID_IS_NULL);

        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = new DevopsCiSonarConfigDTO();
        devopsCiSonarConfigDTO.setStepId(stepId);

        return devopsCiSonarConfigMapper.selectOne(devopsCiSonarConfigDTO);
    }
}
