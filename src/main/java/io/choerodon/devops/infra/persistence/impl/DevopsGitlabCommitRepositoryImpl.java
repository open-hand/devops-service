package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.infra.dataobject.DevopsGitlabCommitDO;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;

@Service
public class DevopsGitlabCommitRepositoryImpl implements DevopsGitlabCommitRepository {

    @Autowired
    DevopsGitlabCommitMapper devopsGitlabCommitMapper;


    @Override
    public DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDO devopsGitlabCommitDO = ConvertHelper.convert(devopsGitlabCommitE, DevopsGitlabCommitDO.class);
        if (devopsGitlabCommitMapper.insert(devopsGitlabCommitDO) != 1) {
            throw new CommonException("error.gitlab.commit.create");
        }
        return ConvertHelper.convert(devopsGitlabCommitDO, DevopsGitlabCommitE.class);
    }
}
