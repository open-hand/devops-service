package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommitE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommitRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommitDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommitMapper;


@Component
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


    @Override
    public DevopsEnvCommitE queryByEnvIdAndCommit(Long envId, String commit) {
        DevopsEnvCommitDO devopsEnvCommitDO = new DevopsEnvCommitDO();
        devopsEnvCommitDO.setEnvId(envId);
        devopsEnvCommitDO.setCommitSha(commit);
        return ConvertHelper.convert(devopsEnvCommitMapper.selectOne(devopsEnvCommitDO), DevopsEnvCommitE.class);
    }

    @Override
    public DevopsEnvCommitE query(Long id) {
        return ConvertHelper.convert(devopsEnvCommitMapper.selectByPrimaryKey(id), DevopsEnvCommitE.class);
    }

    @Override
    public List<DevopsEnvCommitE> listByEnvId(Long envId) {
        DevopsEnvCommitDO devopsEnvCommitDO = new DevopsEnvCommitDO();
        devopsEnvCommitDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsEnvCommitMapper.select(devopsEnvCommitDO), DevopsEnvCommitE.class);
    }

}
