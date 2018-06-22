package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.GitlabCommitDTO;
import io.choerodon.devops.app.service.GitlabCommitService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by Zenger on 2018/5/3.
 */
@Service
public class GitlabCommitServiceImpl implements GitlabCommitService {

    private GitlabProjectRepository gitlabProjectRepository;
    private UserAttrRepository userAttrRepository;

    public GitlabCommitServiceImpl(GitlabProjectRepository gitlabProjectRepository, UserAttrRepository userAttrRepository) {
        this.gitlabProjectRepository = gitlabProjectRepository;
        this.userAttrRepository = userAttrRepository;
    }

    @Override
    public List<GitlabCommitDTO> getGitlabCommit(Integer gitlabProjectId, List<String> shas) {
        List<GitlabCommitE> gitlabCommitEList = new ArrayList<>();
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        shas.parallelStream().forEach(sha -> gitlabCommitEList
                .add(gitlabProjectRepository.getCommit(gitlabProjectId,
                        sha, TypeUtil.objToInteger(userAttrE.getGitlabUserId()))));
        return ConvertHelper.convertList(gitlabCommitEList, GitlabCommitDTO.class);
    }
}
