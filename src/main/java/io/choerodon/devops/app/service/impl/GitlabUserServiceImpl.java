package io.choerodon.devops.app.service.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.GitlabUserRequestDTO;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.config.GitlabConfigurationProperties;
import io.choerodon.devops.infra.dto.UserAttrDTO;
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
    private UserAttrService userAttrService;
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
        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(gitLabUserDTO.getId().longValue());
        if (userAttrDTO == null) {
            userAttrDTO = new UserAttrDTO();
            userAttrDTO.setIamUserId(Long.parseLong(gitlabUserReqDTO.getExternUid()));
            userAttrDTO.setGitlabUserId(gitLabUserDTO.getId().longValue());
            userAttrDTO.setGitlabUserName(gitLabUserDTO.getUsername());
            userAttrService.baseInsert(userAttrDTO);
        }
    }

    @Override
    public void updateGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(gitlabUserReqDTO.getExternUid()));
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.updateUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));
        }
    }

    @Override
    public void isEnabledGitlabUser(Integer userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(userId));
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.enableUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }

    @Override
    public void disEnabledGitlabUser(Integer userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(userId));
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.disableUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
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
