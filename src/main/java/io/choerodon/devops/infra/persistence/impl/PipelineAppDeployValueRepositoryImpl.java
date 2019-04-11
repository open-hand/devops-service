package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployValueE;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployValueRepository;
import io.choerodon.devops.infra.dataobject.PipelineAppDeployValueDO;
import io.choerodon.devops.infra.mapper.PipelineAppDeployValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:14 2019/4/8
 * Description:
 */
@Component
public class PipelineAppDeployValueRepositoryImpl implements PipelineAppDeployValueRepository {
    @Autowired
    private PipelineAppDeployValueMapper deployValueMapper;

    @Override
    public PipelineAppDeployValueE create(PipelineAppDeployValueE appDeployValueE) {
        PipelineAppDeployValueDO appDeployValueDO = ConvertHelper.convert(appDeployValueE, PipelineAppDeployValueDO.class);
        if (deployValueMapper.insert(appDeployValueDO) != 1) {
            throw new CommonException("error.insert.pipeline.app.deploy.value");
        }
        return ConvertHelper.convert(appDeployValueDO, PipelineAppDeployValueE.class);
    }

    @Override
    public PipelineAppDeployValueE update(PipelineAppDeployValueE appDeployValueE) {
        PipelineAppDeployValueDO appDeployValueDO = ConvertHelper.convert(appDeployValueE, PipelineAppDeployValueDO.class);
        if (deployValueMapper.updateByPrimaryKeySelective(appDeployValueDO) != 1) {
            throw new CommonException("error.update.pipeline.app.deploy.value");
        }
        return ConvertHelper.convert(appDeployValueDO, PipelineAppDeployValueE.class);
    }

    @Override
    public PipelineAppDeployValueE queryById(Long valueId) {
        PipelineAppDeployValueDO valueDO = new PipelineAppDeployValueDO();
        valueDO.setId(valueId);
        return ConvertHelper.convert(deployValueMapper.selectByPrimaryKey(valueDO), PipelineAppDeployValueE.class);
    }
}
