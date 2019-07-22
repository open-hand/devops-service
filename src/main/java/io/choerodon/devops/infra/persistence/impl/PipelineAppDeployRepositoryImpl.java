package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.infra.dto.PipelineAppDeployDTO;
import io.choerodon.devops.infra.mapper.PipelineAppDeployMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:12 2019/4/4
 * Description:
 */
@Component
public class PipelineAppDeployRepositoryImpl implements PipelineAppDeployRepository {
    @Autowired
    private PipelineAppDeployMapper appDeployMapper;

    @Override
    public PipelineAppDeployE baseCreate(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDTO appDeployDO = ConvertHelper.convert(pipelineAppDeployE, PipelineAppDeployDTO.class);
        if (appDeployMapper.insert(appDeployDO) != 1) {
            throw new CommonException("error.insert.pipeline.app.deploy");
        }
        return ConvertHelper.convert(appDeployDO, PipelineAppDeployE.class);
    }

    @Override
    public PipelineAppDeployE baseUpdate(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDTO appDeployDO = ConvertHelper.convert(pipelineAppDeployE, PipelineAppDeployDTO.class);
        if (appDeployMapper.updateByPrimaryKeySelective(appDeployDO) != 1) {
            throw new CommonException("error.update.pipeline.app.deploy");
        }
        return ConvertHelper.convert(appDeployMapper.selectByPrimaryKey(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public void baseDeleteById(Long appDelpoyId) {
        appDeployMapper.deleteByPrimaryKey(appDelpoyId);
    }

    @Override
    public PipelineAppDeployE baseQueryById(Long appDelpoyId) {
        return ConvertHelper.convert(appDeployMapper.queryById(appDelpoyId), PipelineAppDeployE.class);
    }

    @Override
    public List<PipelineAppDeployE> baseQueryByAppId(Long appId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setApplicationId(appId);
        return ConvertHelper.convertList(appDeployMapper.select(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public void baseCheckName(String name, Long envId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setInstanceName(name);
        appDeployDO.setEnvId(envId);

        if (appDeployMapper.select(appDeployDO).size() > 0) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }

    @Override
    public List<PipelineAppDeployE> baseQueryByValueId(Long valueId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setValueId(valueId);
        return ConvertHelper.convertList(appDeployMapper.select(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public List<PipelineAppDeployE> baseQueryByEnvId(Long envId) {
        PipelineAppDeployDTO appDeployDO = new PipelineAppDeployDTO();
        appDeployDO.setEnvId(envId);
        return ConvertHelper.convertList(appDeployMapper.select(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public void baseUpdateWithInstanceId(Long instanceId) {
        appDeployMapper.updateInstanceId(instanceId);
    }
}