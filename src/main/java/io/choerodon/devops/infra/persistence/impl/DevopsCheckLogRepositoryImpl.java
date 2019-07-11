package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsCheckLogE;
import io.choerodon.devops.domain.application.repository.DevopsCheckLogRepository;
import io.choerodon.devops.infra.dataobject.DevopsCheckLogDO;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;

@Service
public class DevopsCheckLogRepositoryImpl implements DevopsCheckLogRepository {

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;

    @Override
    public void create(DevopsCheckLogE devopsCheckLogE) {
        devopsCheckLogMapper.insert(ConvertHelper.convert(devopsCheckLogE, DevopsCheckLogDO.class));
    }

    @Override
    public List<DevopsProjectDTO> queryNonEnvGroupProject() {
        return devopsCheckLogMapper.queryNonEnvGroupProject();
    }

    @Override
    public void syncCommandId() {
        devopsCheckLogMapper.syncCommandId();
    }

    @Override
    public void syncCommandVersionId(){
        devopsCheckLogMapper.syncCommandVersionId();
    }
}
