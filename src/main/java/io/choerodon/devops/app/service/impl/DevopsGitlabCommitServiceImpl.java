package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;


@Service
public class DevopsGitlabCommitServiceImpl implements DevopsGitlabCommitService {

    @Autowired
    DevopsGitlabCommitRepository devopsGitlabCommitRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    UserAttrRepository userAttrRepository;
    @Autowired
    IamRepository iamRepository;

    @Override
    public void create(PushWebHookDTO pushWebHookDTO, String token) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        pushWebHookDTO.getCommits().parallelStream().forEach(commitDTO -> {
            DevopsGitlabCommitE devopsGitlabCommitE = new DevopsGitlabCommitE();
            devopsGitlabCommitE.setAppId(applicationE.getId());
            devopsGitlabCommitE.setCommitContent(commitDTO.getMessage());
            devopsGitlabCommitE.setCommitSha(commitDTO.getId());
            devopsGitlabCommitE.setRef(pushWebHookDTO.getRef().split("/")[2]);
            UserE userE = iamRepository.queryByEmail(applicationE.getProjectE().getId(), commitDTO.getAuthor().getEmail());
            if (userE != null) {
                devopsGitlabCommitE.setUserId(userE.getId());
            }
            devopsGitlabCommitE.setCommitDate(commitDTO.getTimestamp());
            devopsGitlabCommitRepository.create(devopsGitlabCommitE);
        });
    }
}

