package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvCommitService;
import io.choerodon.devops.infra.dto.DevopsEnvCommitDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommitMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:56 2019/7/12
 * Description:
 */
@Service
public class DevopsEnvCommitServiceImpl implements DevopsEnvCommitService {
    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper;


    @Override
    public DevopsEnvCommitDTO baseCreate(DevopsEnvCommitDTO devopsEnvCommitDTO) {
        if (devopsEnvCommitMapper.insert(devopsEnvCommitDTO) != 1) {
            throw new CommonException("error.devops.env.commit.create");
        }
        return devopsEnvCommitDTO;
    }


    @Override
    public DevopsEnvCommitDTO baseQueryByEnvIdAndCommit(Long envId, String commit) {
        DevopsEnvCommitDTO devopsEnvCommitDTO = new DevopsEnvCommitDTO();
        devopsEnvCommitDTO.setEnvId(envId);
        devopsEnvCommitDTO.setCommitSha(commit);
        return devopsEnvCommitMapper.selectOne(devopsEnvCommitDTO);
    }

    @Override
    public DevopsEnvCommitDTO baseQuery(Long id) {
        return devopsEnvCommitMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<DevopsEnvCommitDTO> baseListByEnvId(Long envId) {
        DevopsEnvCommitDTO devopsEnvCommitDTO = new DevopsEnvCommitDTO();
        devopsEnvCommitDTO.setEnvId(envId);
        return devopsEnvCommitMapper.select(devopsEnvCommitDTO);
    }
}
