package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineAppDeployService;
import io.choerodon.devops.infra.dto.PipelineAppDeployDTO;
import io.choerodon.devops.infra.mapper.PipelineAppDeployMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:05 2019/7/15
 * Description:
 */
@Service
public class PipelineAppDeployServiceImpl implements PipelineAppDeployService {
    @Autowired
    private PipelineAppDeployMapper appDeployMapper;

    @Override
    public PipelineAppDeployDTO baseCreate(PipelineAppDeployDTO pipelineAppDeployDTO) {
        if (appDeployMapper.insert(pipelineAppDeployDTO) != 1) {
            throw new CommonException("error.insert.pipeline.app.deploy");
        }
        return pipelineAppDeployDTO;
    }

    @Override
    public PipelineAppDeployDTO baseUpdate(PipelineAppDeployDTO pipelineAppDeployDTO) {
        if (appDeployMapper.updateByPrimaryKeySelective(pipelineAppDeployDTO) != 1) {
            throw new CommonException("error.update.pipeline.app.deploy");
        }
        return pipelineAppDeployDTO;
    }

    @Override
    public void baseDeleteById(Long appDelpoyId) {
        appDeployMapper.deleteByPrimaryKey(appDelpoyId);
    }

    @Override
    public PipelineAppDeployDTO baseQueryById(Long appDelpoyId) {
        return appDeployMapper.queryById(appDelpoyId);
    }

    @Override
    public List<PipelineAppDeployDTO> baseQueryByAppId(Long appId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setApplicationId(appId);
        return appDeployMapper.select(appDeployDO);
    }

    @Override
    public void baseCheckName(String name, Long envId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setInstanceName(name);
        appDeployDO.setEnvId(envId);
        if (appDeployMapper.selectOne(appDeployDO) == null) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }

    @Override
    public List<PipelineAppDeployDTO> baseQueryByValueId(Long valueId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setValueId(valueId);
        return appDeployMapper.select(appDeployDO);
    }

    @Override
    public List<PipelineAppDeployDTO> baseQueryByEnvId(Long envId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setEnvId(envId);
        return appDeployMapper.select(appDeployDO);
    }

    @Override
    public void baseUpdateWithInstanceId(Long instanceId) {
        appDeployMapper.updateInstanceId(instanceId);
    }
}
