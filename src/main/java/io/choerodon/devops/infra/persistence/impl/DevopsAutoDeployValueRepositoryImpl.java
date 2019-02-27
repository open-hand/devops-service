package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployValueE;
import io.choerodon.devops.domain.application.repository.DevopsAutoDeployValueRepository;
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployValueDO;
import io.choerodon.devops.infra.mapper.DevopsAutoDeployValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:29 2019/2/26
 * Description:
 */
@Component
public class DevopsAutoDeployValueRepositoryImpl implements DevopsAutoDeployValueRepository {
    @Autowired
    private DevopsAutoDeployValueMapper devopsAutoDeployValueMapper;

    @Override
    public Long createOrUpdate(DevopsAutoDeployValueE devopsAutoDeployValueE) {
        DevopsAutoDeployValueDO devopsAutoDeployValueDO = ConvertHelper.convert(devopsAutoDeployValueE, DevopsAutoDeployValueDO.class);

        if (devopsAutoDeployValueDO.getId() == null) {
            if (devopsAutoDeployValueMapper.insert(devopsAutoDeployValueDO) != 1) {
                throw new CommonException("error.auto.deploy.value.create");
            }
        } else {
            if (devopsAutoDeployValueMapper.updateByPrimaryKeySelective(devopsAutoDeployValueDO) != 1) {
                throw new CommonException("error.auto.deploy.value.update");
            }
        }
        return devopsAutoDeployValueMapper.selectOne(devopsAutoDeployValueDO).getId();
    }
}
