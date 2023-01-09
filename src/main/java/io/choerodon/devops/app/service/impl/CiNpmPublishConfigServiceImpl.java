package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.CiNpmPublishConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiNpmPublishConfigDTO;
import io.choerodon.devops.infra.mapper.CiNpmPublishConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线npm发布配置(CiNpmPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 15:26:47
 */
@Service
public class CiNpmPublishConfigServiceImpl implements CiNpmPublishConfigService {

    private static final String DEVOPS_SAVE_NPM_PUBLISH_CONFIG_FAILED = "devops.save.npm.publish.config.failed";
    @Autowired
    private CiNpmPublishConfigMapper ciNpmPublishConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiNpmPublishConfigDTO npmPublishConfig) {
        npmPublishConfig.setId(null);

        MapperUtil.resultJudgedInsertSelective(ciNpmPublishConfigMapper, npmPublishConfig, DEVOPS_SAVE_NPM_PUBLISH_CONFIG_FAILED);
    }

    @Override
    public CiNpmPublishConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);

        CiNpmPublishConfigDTO ciNpmPublishConfigDTO = new CiNpmPublishConfigDTO();
        ciNpmPublishConfigDTO.setStepId(stepId);

        return ciNpmPublishConfigMapper.selectOne(ciNpmPublishConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        if (!CollectionUtils.isEmpty(stepIds)) {
            ciNpmPublishConfigMapper.batchDeleteByStepIds(stepIds);
        }

    }
}

