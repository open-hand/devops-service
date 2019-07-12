package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommitVO;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommitRepository;
import io.choerodon.devops.infra.dto.DevopsEnvCommitDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommitMapper;


@Component
public class DevopsEnvCommitRepositoryImpl implements DevopsEnvCommitRepository {

    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper;


    @Override
    public DevopsEnvCommitVO baseCreate(DevopsEnvCommitVO devopsEnvCommitE) {
        DevopsEnvCommitDTO devopsEnvCommitDO = ConvertHelper.convert(devopsEnvCommitE, DevopsEnvCommitDTO.class);
        if (devopsEnvCommitMapper.insert(devopsEnvCommitDO) != 1) {
            throw new CommonException("error.devops.env.commit.create");
        }
        return ConvertHelper.convert(devopsEnvCommitDO, DevopsEnvCommitVO.class);
    }


    @Override
    public DevopsEnvCommitVO baseQueryByEnvIdAndCommit(Long envId, String commit) {
        DevopsEnvCommitDTO devopsEnvCommitDO = new DevopsEnvCommitDTO();
        devopsEnvCommitDO.setEnvId(envId);
        devopsEnvCommitDO.setCommitSha(commit);
        return ConvertHelper.convert(devopsEnvCommitMapper.selectOne(devopsEnvCommitDO), DevopsEnvCommitVO.class);
    }

    @Override
    public DevopsEnvCommitVO baseQuery(Long id) {
        return ConvertHelper.convert(devopsEnvCommitMapper.selectByPrimaryKey(id), DevopsEnvCommitVO.class);
    }

    @Override
    public List<DevopsEnvCommitVO> baseListByEnvId(Long envId) {
        DevopsEnvCommitDTO devopsEnvCommitDO = new DevopsEnvCommitDTO();
        devopsEnvCommitDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsEnvCommitMapper.select(devopsEnvCommitDO), DevopsEnvCommitVO.class);
    }

}
