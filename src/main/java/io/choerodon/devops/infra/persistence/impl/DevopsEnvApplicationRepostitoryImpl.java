package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvMessageE;
import io.choerodon.devops.domain.application.repository.DevopsEnvApplicationRepostitory;
import io.choerodon.devops.infra.dataobject.DevopsEnvApplicationDO;
import io.choerodon.devops.infra.mapper.DevopsEnvApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Component
public class DevopsEnvApplicationRepostitoryImpl implements DevopsEnvApplicationRepostitory {

    @Autowired
    DevopsEnvApplicationMapper devopsEnvApplicationMapper;

    @Override
    public DevopsEnvApplicationE create(DevopsEnvApplicationE devopsEnvApplicationE) {
        DevopsEnvApplicationDO devopsEnvApplicationDO = ConvertHelper.convert(devopsEnvApplicationE,DevopsEnvApplicationDO.class);
        if(devopsEnvApplicationMapper.insert(devopsEnvApplicationDO)!= 1){
            throw new CommonException("error.insert.env.app");
        }
        return devopsEnvApplicationE;
    }

    @Override
    public List<Long> queryAppByEnvId(Long envId) {
        return devopsEnvApplicationMapper.queryAppByEnvId(envId);
    }

    @Override
    public List<DevopsEnvMessageE> listResourceByEnvAndApp(Long envId, Long appId) {
        return devopsEnvApplicationMapper.listResourceByEnvAndApp(envId,appId);
    }
}
