package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileDO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileMapper;

@Component
public class DevopsEnvFileRepositoryImpl implements DevopsEnvFileRepository {

    @Autowired
    DevopsEnvFileMapper devopsEnvFileMapper;

    @Override
    public DevopsEnvFileE create(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDO devopsEnvFileDO = ConvertHelper.convert(devopsEnvFileE, DevopsEnvFileDO.class);
        if (devopsEnvFileMapper.insert(devopsEnvFileDO) != 1) {
            throw new CommonException("error.env.file.create");
        }
        return ConvertHelper.convert(devopsEnvFileDO, DevopsEnvFileE.class);
    }

    @Override
    public List<DevopsEnvFileE> listByEnvId(Long envId) {
        DevopsEnvFileDO devopsEnvFileDO = new DevopsEnvFileDO();
        devopsEnvFileDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsEnvFileMapper.select(devopsEnvFileDO), DevopsEnvFileE.class);
    }

    @Override
    public DevopsEnvFileE queryByEnvAndPath(Long envId, String path) {
        DevopsEnvFileDO devopsEnvFileDO = new DevopsEnvFileDO();
        devopsEnvFileDO.setEnvId(envId);
        devopsEnvFileDO.setFilePath(path);
        return ConvertHelper.convert(devopsEnvFileMapper.selectOne(devopsEnvFileDO), DevopsEnvFileE.class);
    }

    @Override
    public DevopsEnvFileE queryByEnvAndPathAndCommit(Long envId, String path, String commit) {
        DevopsEnvFileDO devopsEnvFileDO = new DevopsEnvFileDO();
        devopsEnvFileDO.setEnvId(envId);
        devopsEnvFileDO.setFilePath(path);
        devopsEnvFileDO.setDevopsCommit(commit);
        return ConvertHelper.convert(devopsEnvFileMapper.selectOne(devopsEnvFileDO), DevopsEnvFileE.class);
    }

    @Override
    public DevopsEnvFileE queryByEnvAndPathAndCommits(Long envId, String path, List<String> commits) {
        return ConvertHelper
                .convert(devopsEnvFileMapper.queryByEnvAndPathAndCommits(envId, path, commits), DevopsEnvFileE.class);
    }

    @Override
    public void update(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDO devopsEnvFileDO = devopsEnvFileMapper.selectByPrimaryKey(devopsEnvFileE.getId());
        devopsEnvFileDO.setDevopsCommit(devopsEnvFileE.getDevopsCommit());
        devopsEnvFileDO.setAgentCommit(devopsEnvFileE.getAgentCommit());
        devopsEnvFileMapper.updateByPrimaryKeySelective(devopsEnvFileDO);
    }

    @Override
    public void delete(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDO devopsEnvFileDO = ConvertHelper.convert(devopsEnvFileE, DevopsEnvFileDO.class);
        devopsEnvFileMapper.delete(devopsEnvFileDO);
    }

    @Override
    public List<DevopsEnvFileE> listByEnvIdAndPath(Long envId, String path) {
        DevopsEnvFileDO devopsEnvFileDO = new DevopsEnvFileDO();
        devopsEnvFileDO.setEnvId(envId);
        devopsEnvFileDO.setFilePath(path);
        return ConvertHelper.convertList(devopsEnvFileMapper.select(devopsEnvFileDO), DevopsEnvFileE.class);
    }
}
