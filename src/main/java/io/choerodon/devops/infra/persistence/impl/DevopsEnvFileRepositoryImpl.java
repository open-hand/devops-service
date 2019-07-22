package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileRepository;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileMapper;

@Component
public class DevopsEnvFileRepositoryImpl implements DevopsEnvFileRepository {

    @Autowired
    DevopsEnvFileMapper devopsEnvFileMapper;

    @Override
    public DevopsEnvFileE baseCreate(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDTO devopsEnvFileDO = ConvertHelper.convert(devopsEnvFileE, DevopsEnvFileDTO.class);
        if (devopsEnvFileMapper.insert(devopsEnvFileDO) != 1) {
            throw new CommonException("error.env.file.create");
        }
        return ConvertHelper.convert(devopsEnvFileDO, DevopsEnvFileE.class);
    }

    @Override
    public List<DevopsEnvFileE> baseListByEnvId(Long envId) {
        DevopsEnvFileDTO devopsEnvFileDO = new DevopsEnvFileDTO();
        devopsEnvFileDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsEnvFileMapper.select(devopsEnvFileDO), DevopsEnvFileE.class);
    }

    @Override
    public DevopsEnvFileE baseQueryByEnvAndPath(Long envId, String path) {
        DevopsEnvFileDTO devopsEnvFileDO = new DevopsEnvFileDTO();
        devopsEnvFileDO.setEnvId(envId);
        devopsEnvFileDO.setFilePath(path);
        return ConvertHelper.convert(devopsEnvFileMapper.selectOne(devopsEnvFileDO), DevopsEnvFileE.class);
    }

    @Override
    public DevopsEnvFileE baseQueryByEnvAndPathAndCommit(Long envId, String path, String commit) {
        DevopsEnvFileDTO devopsEnvFileDO = new DevopsEnvFileDTO();
        devopsEnvFileDO.setEnvId(envId);
        devopsEnvFileDO.setFilePath(path);
        devopsEnvFileDO.setDevopsCommit(commit);
        return ConvertHelper.convert(devopsEnvFileMapper.selectOne(devopsEnvFileDO), DevopsEnvFileE.class);
    }

    @Override
    public DevopsEnvFileE baseQueryByEnvAndPathAndCommits(Long envId, String path, List<String> commits) {
        return ConvertHelper
                .convert(devopsEnvFileMapper.queryByEnvAndPathAndCommits(envId, path, commits), DevopsEnvFileE.class);
    }

    @Override
    public void baseUpdate(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDTO devopsEnvFileDO = devopsEnvFileMapper.selectByPrimaryKey(devopsEnvFileE.getId());
        devopsEnvFileDO.setDevopsCommit(devopsEnvFileE.getDevopsCommit());
        devopsEnvFileDO.setAgentCommit(devopsEnvFileE.getAgentCommit());
        devopsEnvFileMapper.updateByPrimaryKeySelective(devopsEnvFileDO);
    }

    @Override
    public void baseDelete(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDTO devopsEnvFileDO = ConvertHelper.convert(devopsEnvFileE, DevopsEnvFileDTO.class);
        devopsEnvFileMapper.delete(devopsEnvFileDO);
    }

    @Override
    public List<DevopsEnvFileE> baseListByEnvIdAndPath(Long envId, String path) {
        DevopsEnvFileDTO devopsEnvFileDO = new DevopsEnvFileDTO();
        devopsEnvFileDO.setEnvId(envId);
        devopsEnvFileDO.setFilePath(path);
        return ConvertHelper.convertList(devopsEnvFileMapper.select(devopsEnvFileDO), DevopsEnvFileE.class);
    }
}
