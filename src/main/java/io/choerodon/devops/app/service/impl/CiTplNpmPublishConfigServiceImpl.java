package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiTplNpmPublishConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiTplNpmPublishConfigDTO;
import io.choerodon.devops.infra.mapper.CiTplNpmPublishConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线模板npm发布配置(CiTplNpmPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 17:46:47
 */
@Service
public class CiTplNpmPublishConfigServiceImpl implements CiTplNpmPublishConfigService {

    @Autowired
    private static final String DEVOPS_SAVE_NPM_PUBLISH_CONFIG_FAILED = "devops.save.npm.publish.config.failed";

    @Autowired
    private CiTplNpmPublishConfigMapper ciTplNpmPublishConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO) {
        ciTplNpmPublishConfigDTO.setId(null);
        MapperUtil.resultJudgedInsertSelective(ciTplNpmPublishConfigMapper, ciTplNpmPublishConfigDTO, DEVOPS_SAVE_NPM_PUBLISH_CONFIG_FAILED);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateStepId(Long templateStepId) {
        Assert.notNull(templateStepId, PipelineCheckConstant.DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL);

        CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO = new CiTplNpmPublishConfigDTO();
        ciTplNpmPublishConfigDTO.setCiTemplateStepId(templateStepId);

        ciTplNpmPublishConfigMapper.delete(ciTplNpmPublishConfigDTO);
    }

    @Override
    public CiTplNpmPublishConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL);

        CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO = new CiTplNpmPublishConfigDTO();
        ciTplNpmPublishConfigDTO.setCiTemplateStepId(stepId);
        return ciTplNpmPublishConfigMapper.selectOne(ciTplNpmPublishConfigDTO);
    }
}

