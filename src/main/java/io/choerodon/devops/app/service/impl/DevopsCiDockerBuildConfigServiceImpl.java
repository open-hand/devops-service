package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;
import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsCiDockerBuildConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:25
 */
@Service
public class DevopsCiDockerBuildConfigServiceImpl implements DevopsCiDockerBuildConfigService {
    @Autowired
    private DevopsCiDockerBuildConfigMapper devopsCiDockerBuildConfigMapper;

    @Override
    public DevopsCiDockerBuildConfigDTO baseQuery(Long id) {
        return devopsCiDockerBuildConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiDockerBuildConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, "error.step.id.is.null");
        DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = new DevopsCiDockerBuildConfigDTO();
        devopsCiDockerBuildConfigDTO.setStepId(stepId);

        return devopsCiDockerBuildConfigMapper.selectOne(devopsCiDockerBuildConfigDTO);
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiDockerBuildConfigMapper,
                devopsCiDockerBuildConfigDTO,
                "error.save.docker.build.config.failed");
    }

    @Override
    @Transactional
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        devopsCiDockerBuildConfigMapper.batchDeleteByStepIds(stepIds);
    }
}
