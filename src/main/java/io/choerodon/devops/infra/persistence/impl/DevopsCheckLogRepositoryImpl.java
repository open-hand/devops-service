package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.repository.DevopsCheckLogRepository;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;

@Service
public class DevopsCheckLogRepositoryImpl implements DevopsCheckLogRepository {

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;

    @Override
    public void baseCreateLog(DevopsCheckLogE devopsCheckLogE) {
        devopsCheckLogMapper.insert(ConvertHelper.convert(devopsCheckLogE, DevopsCheckLogDTO.class));
    }

    @Override
    public List<DevopsProjectDTO> baseQueryNonEnvGroupProject() {
        return devopsCheckLogMapper.queryNonEnvGroupProject();
    }

    @Override
    public void baseSyncCommandId() {
        devopsCheckLogMapper.syncCommandId();
    }

    @Override
    public void baseSyncCommandVersionId(){
        devopsCheckLogMapper.syncCommandVersionId();
    }
}
