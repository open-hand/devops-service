package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.CiNpmBuildConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiNpmBuildConfigDTO;
import io.choerodon.devops.infra.mapper.CiNpmBuildConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线npm构建配置(CiNpmBuildConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-11 10:42:11
 */
@Service
public class CiNpmBuildConfigServiceImpl implements CiNpmBuildConfigService {

    private static final String DEVOPS_SAVE_NPM_BUILD_CONFIG_FAILED = "devops.save.npm.build.config.failed";
    @Autowired
    private CiNpmBuildConfigMapper ciNpmBuildConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiNpmBuildConfigDTO npmBuildConfig) {
        npmBuildConfig.setId(null);

        MapperUtil.resultJudgedInsertSelective(ciNpmBuildConfigMapper, npmBuildConfig, DEVOPS_SAVE_NPM_BUILD_CONFIG_FAILED);
    }

    @Override
    public CiNpmBuildConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);

        CiNpmBuildConfigDTO ciNpmBuildConfigDTO = new CiNpmBuildConfigDTO();
        ciNpmBuildConfigDTO.setStepId(stepId);

        return ciNpmBuildConfigMapper.selectOne(ciNpmBuildConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        if (!CollectionUtils.isEmpty(stepIds)) {
            ciNpmBuildConfigMapper.batchDeleteByStepIds(stepIds);
        }
    }
}

