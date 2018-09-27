package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.GitlabUserRequestDTO;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.domain.application.repository.GitlabUserRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.config.GitlabConfigurationProperties;

/**
 * Created by Zenger on 2018/3/28.
 */
@Service
public class GitlabUserServiceImpl implements GitlabUserService {
    @Autowired
    private GitlabConfigurationProperties gitlabConfigurationProperties;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


    @Override
    public void createGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {

        GitlabUserE createOrUpdateGitlabUserE = gitlabUserRepository.createGitLabUser(
                gitlabConfigurationProperties.getPassword(),
                gitlabConfigurationProperties.getProjectLimit(),
                ConvertHelper.convert(gitlabUserReqDTO, GitlabUserEvent.class));

        if (createOrUpdateGitlabUserE != null) {
            UserAttrE userAttrE = new UserAttrE();
            userAttrE.setId(Long.parseLong(gitlabUserReqDTO.getExternUid()));
            userAttrE.setGitlabUserId(createOrUpdateGitlabUserE.getId().longValue());
            userAttrRepository.insert(userAttrE);
        }
    }

    @Override
    public void updateGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(gitlabUserReqDTO.getExternUid()));
        if (userAttrE != null) {

            gitlabUserRepository.updateGitLabUser(TypeUtil.objToInteger(userAttrE.getGitlabUserId()),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertHelper.convert(gitlabUserReqDTO, GitlabUserEvent.class));
        }
    }

    @Override
    public void isEnabledGitlabUser(Integer userId) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(userId));
        if (userAttrE != null) {
            gitlabUserRepository.isEnabledGitlabUser(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }

    @Override
    public void disEnabledGitlabUser(Integer userId) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(userId));
        if (userAttrE != null) {
            gitlabUserRepository.disEnabledGitlabUser(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }
}
