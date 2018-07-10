package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.entity.gitlab.MergeRequestE;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;


@Service
public class DevopsMergeRequestRepositoryImpl implements DevopsMergeRequestRepository {

    @Autowired
    DevopsMergeRequestMapper devopsMergeRequestMapper;

    @Override
    public Integer create(DevopsMergeRequestE devopsMergeRequestE) {
        DevopsMergeRequestDO devopsMergeRequestDO = ConvertHelper.convert(devopsMergeRequestE,
                DevopsMergeRequestDO.class);
        return devopsMergeRequestMapper.insert(devopsMergeRequestDO);
    }

    @Override
    public List<MergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId) {
        DevopsMergeRequestDO mergeRequestDO = new DevopsMergeRequestDO();
        mergeRequestDO.setSourceBranch(sourceBranchName);
        mergeRequestDO.setProjectId(gitLabProjectId);
        return ConvertHelper.convertList(devopsMergeRequestMapper.select(mergeRequestDO), MergeRequestE.class);
    }

    @Override
    public Integer queryByAppIdAndGitlabId(Long applicationId, Long gitlabMergeRequestId) {
        return devopsMergeRequestMapper.queryByAppIdAndGitlabId(applicationId, gitlabMergeRequestId);
    }

}
