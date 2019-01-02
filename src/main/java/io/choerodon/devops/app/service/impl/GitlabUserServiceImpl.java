package io.choerodon.devops.app.service.impl;

import java.util.regex.Pattern;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zenger on 2018/3/28.
 */
@Service
public class GitlabUserServiceImpl implements GitlabUserService {
    private static final String SERVICE_PATTERN = "[a-zA-Z0-9_\\.][a-zA-Z0-9_\\-\\.]*[a-zA-Z0-9_\\-]|[a-zA-Z0-9_]";

    @Autowired
    private GitlabConfigurationProperties gitlabConfigurationProperties;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


    @Override
    public void createGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        GitlabUserE gitlabUserE = gitlabUserRepository.getUserByUserName(gitlabUserReqDTO.getUsername());
        if (gitlabUserE == null) {
            gitlabUserE = gitlabUserRepository.createGitLabUser(
                    gitlabConfigurationProperties.getPassword(),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertHelper.convert(gitlabUserReqDTO, GitlabUserEvent.class));
        }
        if (gitlabUserE != null) {
            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(gitlabUserE.getId().longValue());
            if (userAttrE == null) {
                userAttrE.setIamUserId(Long.parseLong(gitlabUserReqDTO.getExternUid()));
                userAttrE.setGitlabUserId(gitlabUserE.getId().longValue());
                userAttrE.setGitlabUserName(gitlabUserE.getUsername());
                userAttrRepository.insert(userAttrE);
            }
        }
    }

    @Override
    public void updateGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
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


    private void checkGitlabUser(GitlabUserRequestDTO gitlabUserRequestDTO) {
        String userName = gitlabUserRequestDTO.getUsername();
        String newUserName = "";
        for (int i = 0; i < userName.length(); i++) {
            if (!Pattern.matches(SERVICE_PATTERN, String.valueOf(userName.charAt(i)))) {
                newUserName += "_";
            } else {
                newUserName += String.valueOf(userName.charAt(i));
            }
        }
        gitlabUserRequestDTO.setUsername(newUserName);
    }
}
