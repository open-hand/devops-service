package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiDockerAuthConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiDockerAuthConfigDTO;
import io.choerodon.devops.infra.mapper.CiDockerAuthConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线配置的docker认证配置(CiDockerAuthConfig)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-15 09:54:20
 */
@Service
public class CiDockerAuthConfigServiceImpl implements CiDockerAuthConfigService {

    private static final String DEVOPS_SAVE_DOCKER_AUTH_CONFIG = "devops.save.docker.auth.config";

    @Autowired
    private CiDockerAuthConfigMapper ciDockerAuthConfigMapper;


    @Override
    @Transactional
    public void baseCreate(CiDockerAuthConfigDTO ciDockerAuthConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(ciDockerAuthConfigMapper, ciDockerAuthConfigDTO, DEVOPS_SAVE_DOCKER_AUTH_CONFIG);
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);

        CiDockerAuthConfigDTO ciDockerAuthConfigDTO = new CiDockerAuthConfigDTO();
        ciDockerAuthConfigDTO.setDevopsPipelineId(pipelineId);
        ciDockerAuthConfigMapper.delete(ciDockerAuthConfigDTO);
    }

    @Override
    public List<CiDockerAuthConfigDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);

        CiDockerAuthConfigDTO ciDockerAuthConfigDTO = new CiDockerAuthConfigDTO();
        ciDockerAuthConfigDTO.setDevopsPipelineId(pipelineId);
        return ciDockerAuthConfigMapper.select(ciDockerAuthConfigDTO);
    }
}

