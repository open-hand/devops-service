package io.choerodon.devops.app.service.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.GitlabUserRequestDTO;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.config.GitlabConfigurationProperties;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabUserReqDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Zenger on 2018/3/28.
 */
@Service
public class GitlabUserServiceImpl implements GitlabUserService {
    private static final String SERVICE_PATTERN = "[a-zA-Z0-9_\\.][a-zA-Z0-9_\\-\\.]*[a-zA-Z0-9_\\-]|[a-zA-Z0-9_]";

    @Autowired
    private GitlabConfigurationProperties gitlabConfigurationProperties;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;


    @Override
    public void createGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserByUserName(gitlabUserReqDTO.getUsername());
        if (gitLabUserDTO == null) {
            gitLabUserDTO = gitlabServiceClientOperator.createUser(
                    gitlabConfigurationProperties.getPassword(),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));
        }
        UserAttrE userAttrE = userAttrRepository.baseQueryByGitlabUserId(gitLabUserDTO.getId().longValue());
        if (userAttrE == null) {
            userAttrE = new UserAttrE();
            userAttrE.setIamUserId(Long.parseLong(gitlabUserReqDTO.getExternUid()));
            userAttrE.setGitlabUserId(gitLabUserDTO.getId().longValue());
            userAttrE.setGitlabUserName(gitLabUserDTO.getUsername());
            userAttrRepository.baseInsert(userAttrE);
        }
    }

    @Override
    public void updateGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(gitlabUserReqDTO.getExternUid()));
        if (userAttrE != null) {
            gitlabServiceClientOperator.updateUser(TypeUtil.objToInteger(userAttrE.getGitlabUserId()),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));
        }
    }

    @Override
    public void isEnabledGitlabUser(Integer userId) {
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(userId));
        if (userAttrE != null) {
            gitlabServiceClientOperator.enableUser(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }

    @Override
    public void disEnabledGitlabUser(Integer userId) {
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(userId));
        if (userAttrE != null) {
            gitlabServiceClientOperator.disableUser(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }


    private void checkGitlabUser(GitlabUserRequestDTO gitlabUserRequestDTO) {
        String userName = gitlabUserRequestDTO.getUsername();
        StringBuilder newUserName = new StringBuilder();
        for (int i = 0; i < userName.length(); i++) {
            if (!Pattern.matches(SERVICE_PATTERN, String.valueOf(userName.charAt(i)))) {
                newUserName.append("_");
            } else {
                newUserName.append(String.valueOf(userName.charAt(i)));
            }
        }
        gitlabUserRequestDTO.setUsername(newUserName.toString());
    }

    @Override
    public Boolean doesEmailExists(String email) {
        return gitlabServiceClientOperator.checkEmail(email);
    }

    @Override
    public GitLabUserDTO getGitlabUserByUserId(Integer userId) {
        return gitlabServiceClientOperator.queryUserById(userId);
    }
}
