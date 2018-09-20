package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;

public interface DevopsGitlabCommitRepository {

    DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE);


    DevopsGitlabCommitE queryBySha(String sha);
}
