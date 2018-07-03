package io.choerodon.devops.domain.service;

import java.util.List;

import io.choerodon.devops.domain.application.entity.gitlab.GitFlowE;

public interface IDevopsGitService {

    public List<GitFlowE> getMergeRequestList();
}
