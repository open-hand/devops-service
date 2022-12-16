package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.infra.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.infra.dto.CiTemplateDockerDTO;
import io.choerodon.devops.infra.mapper.CiTemplateDockerMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateDocker)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:56:48
 */
@Service
public class CiTemplateDockerServiceImpl implements CiTemplateDockerService {
    private static final String DEVOPS_SAVE_DOCKER_BUILD_CONFIG_FAILED = "devops.tpl.save.docker.build.config.failed";

    @Autowired
    private CiTemplateDockerMapper ciTemplateDockermapper;


    @Override
    public CiTemplateDockerDTO queryByStepId(Long stepId) {
        CiTemplateDockerDTO ciTemplateDockerDTO = new CiTemplateDockerDTO();
        ciTemplateDockerDTO.setCiTemplateStepId(stepId);

        return ciTemplateDockermapper.selectOne(ciTemplateDockerDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiTemplateDockerDTO dockerBuildConfig) {
        MapperUtil.resultJudgedInsertSelective(ciTemplateDockermapper,
                dockerBuildConfig, DEVOPS_SAVE_DOCKER_BUILD_CONFIG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateId(Long templateStepId) {
        CiTemplateDockerDTO ciTemplateDockerDTO = new CiTemplateDockerDTO();
        ciTemplateDockerDTO.setCiTemplateStepId(templateStepId);
        ciTemplateDockermapper.delete(ciTemplateDockerDTO);
    }
}

