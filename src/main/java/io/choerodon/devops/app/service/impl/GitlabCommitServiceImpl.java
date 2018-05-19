package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.GitlabCommitDTO;
import io.choerodon.devops.app.service.GitlabCommitService;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;

/**
 * Created by Zenger on 2018/5/3.
 */
@Service
public class GitlabCommitServiceImpl implements GitlabCommitService {

    private GitlabProjectRepository gitlabProjectRepository;

    public GitlabCommitServiceImpl(GitlabProjectRepository gitlabProjectRepository) {
        this.gitlabProjectRepository = gitlabProjectRepository;
    }

    @Override
    public List<GitlabCommitDTO> getGitlabCommit(Integer gitlabProjectId, List<String> shas) {
        List<GitlabCommitE> gitlabCommitEList = new ArrayList<>();
        String username = GitUserNameUtil.getUsername();
        shas.parallelStream().forEach(sha -> {
            gitlabCommitEList.add(gitlabProjectRepository.getCommit(gitlabProjectId,
                    sha, username));
        });
        return ConvertHelper.convertList(gitlabCommitEList, GitlabCommitDTO.class);
    }
}
