package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
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
}