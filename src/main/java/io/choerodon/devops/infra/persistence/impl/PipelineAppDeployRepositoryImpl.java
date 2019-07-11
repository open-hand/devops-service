package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.infra.dataobject.PipelineAppDeployDO;
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
    public PipelineAppDeployE create(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDO appDeployDO = ConvertHelper.convert(pipelineAppDeployE, PipelineAppDeployDO.class);
        if (appDeployMapper.insert(appDeployDO) != 1) {
            throw new CommonException("error.insert.pipeline.app.deploy");
        }
        return ConvertHelper.convert(appDeployDO, PipelineAppDeployE.class);
    }

    @Override
    public PipelineAppDeployE update(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDO appDeployDO = ConvertHelper.convert(pipelineAppDeployE, PipelineAppDeployDO.class);
        if (appDeployMapper.updateByPrimaryKeySelective(appDeployDO) != 1) {
            throw new CommonException("error.update.pipeline.app.deploy");
        }
        return ConvertHelper.convert(appDeployMapper.selectByPrimaryKey(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public void deleteById(Long appDelpoyId) {
        appDeployMapper.deleteByPrimaryKey(appDelpoyId);
    }

    @Override
    public PipelineAppDeployE queryById(Long appDelpoyId) {
        return ConvertHelper.convert(appDeployMapper.queryById(appDelpoyId), PipelineAppDeployE.class);
    }

    @Override
    public List<PipelineAppDeployE> queryByAppId(Long appId) {
        PipelineAppDeployDO appDeployDO = new PipelineAppDeployDO();
        appDeployDO.setApplicationId(appId);
        return ConvertHelper.convertList(appDeployMapper.select(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public void checkName(String name, Long envId) {
        PipelineAppDeployDO appDeployDO = new PipelineAppDeployDO();
        appDeployDO.setInstanceName(name);
        appDeployDO.setEnvId(envId);

        if (appDeployMapper.select(appDeployDO).size() > 0) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }

    @Override
    public List<PipelineAppDeployE> queryByValueId(Long valueId) {
        PipelineAppDeployDO appDeployDO = new PipelineAppDeployDO();
        appDeployDO.setValueId(valueId);
        return ConvertHelper.convertList(appDeployMapper.select(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public List<PipelineAppDeployE> queryByEnvId(Long envId) {
        PipelineAppDeployDO appDeployDO = new PipelineAppDeployDO();
        appDeployDO.setEnvId(envId);
        return ConvertHelper.convertList(appDeployMapper.select(appDeployDO), PipelineAppDeployE.class);
    }

    @Override
    public void updateInstanceId(Long instanceId) {
        appDeployMapper.updateInstanceId(instanceId);
    }
}