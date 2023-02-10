package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiTplNpmBuildConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiTplNpmBuildConfigDTO;
import io.choerodon.devops.infra.mapper.CiTplNpmBuildConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线模板npm发布配置(CiTplNpmBuildConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-11 10:42:25
 */
@Service
public class CiTplNpmBuildConfigServiceImpl implements CiTplNpmBuildConfigService {
    private static final String DEVOPS_SAVE_TPL_NPM_BUILD_CONFIG_FAILED = "devops.save.tpl.npm.build.config.failed";
    @Autowired
    private CiTplNpmBuildConfigMapper ciTplNpmBuildConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO) {
        ciTplNpmBuildConfigDTO.setId(null);

        MapperUtil.resultJudgedInsertSelective(ciTplNpmBuildConfigMapper, ciTplNpmBuildConfigDTO, DEVOPS_SAVE_TPL_NPM_BUILD_CONFIG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateStepId(Long templateStepId) {
        Assert.notNull(templateStepId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);

        CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO = new CiTplNpmBuildConfigDTO();
        ciTplNpmBuildConfigDTO.setCiTemplateStepId(templateStepId);

        ciTplNpmBuildConfigMapper.delete(ciTplNpmBuildConfigDTO);
    }

    @Override
    public CiTplNpmBuildConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);

        CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO = new CiTplNpmBuildConfigDTO();
        ciTplNpmBuildConfigDTO.setCiTemplateStepId(stepId);

        return ciTplNpmBuildConfigMapper.selectOne(ciTplNpmBuildConfigDTO);
    }
}

