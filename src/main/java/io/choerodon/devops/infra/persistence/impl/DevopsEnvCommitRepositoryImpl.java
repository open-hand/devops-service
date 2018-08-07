package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommitE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommitRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommitDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommitMapper;

public class DevopsEnvCommitRepositoryImpl implements DevopsEnvCommitRepository {

    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper;


    @Override
    public DevopsEnvCommitE create(DevopsEnvCommitE devopsEnvCommitE) {
        DevopsEnvCommitDO devopsEnvCommitDO = ConvertHelper.convert(devopsEnvCommitE, DevopsEnvCommitDO.class);
        if (devopsEnvCommitMapper.insert(devopsEnvCommitDO) != 1) {
            throw new CommonException("error.devops.env.commit.create");
        }
        return ConvertHelper.convert(devopsEnvCommitDO, DevopsEnvCommitE.class);
    }
}
