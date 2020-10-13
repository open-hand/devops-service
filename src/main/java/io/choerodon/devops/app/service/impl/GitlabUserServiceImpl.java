package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.config.GitlabConfigurationProperties;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabUserReqDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GenerateUUID;
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
    @Autowired
    private SendNotificationService sendNotificationService;


    @Override
    public void createGitlabUser(GitlabUserRequestVO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserByUserName(gitlabUserReqDTO.getUsername());
        if (gitLabUserDTO == null) {
            String randomPassword = GenerateUUID.generateRandomGitlabPassword();

            gitLabUserDTO = gitlabServiceClientOperator.createUser(
                    randomPassword,
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));

            // 以通知形式告知默认密码
            sendNotificationService.sendForUserDefaultPassword(gitlabUserReqDTO.getExternUid(), randomPassword);
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
    public void updateGitlabUser(GitlabUserRequestVO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(gitlabUserReqDTO.getExternUid()));
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.updateUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));
        }
    }

    @Override
    public void isEnabledGitlabUser(Long userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.enableUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }

    @Override
    public void disEnabledGitlabUser(Long userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.disableUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }


    private void checkGitlabUser(GitlabUserRequestVO gitlabUserRequestVO) {
        String userName = gitlabUserRequestVO.getUsername();
        StringBuilder newUserName = new StringBuilder();
        for (int i = 0; i < userName.length(); i++) {
            if (!Pattern.matches(SERVICE_PATTERN, String.valueOf(userName.charAt(i)))) {
                newUserName.append("_");
            } else {
                newUserName.append(String.valueOf(userName.charAt(i)));
            }
        }
        gitlabUserRequestVO.setUsername(newUserName.toString());
    }

    @Override
    public Boolean doesEmailExists(String email) {
        return gitlabServiceClientOperator.checkEmail(email);
    }

    @Override
    public void assignAdmins(List<Long> iamUserIds) {
        if (CollectionUtils.isEmpty(iamUserIds)) {
            return;
        }

        iamUserIds.stream()
                .map(iamUserId -> userAttrService.checkUserSync(userAttrService.baseQueryById(iamUserId), iamUserId))
                .forEach(user -> {
                    gitlabServiceClientOperator.assignAdmin(user.getIamUserId(), TypeUtil.objToInteger(user.getGitlabUserId()));
                    userAttrService.updateAdmin(user.getIamUserId(), Boolean.TRUE);
                });
    }

    @Override
    public void deleteAdmin(Long iamUserId) {
        if (iamUserId == null) {
            return;
        }

        UserAttrDTO userAttrDTO = userAttrService.checkUserSync(userAttrService.baseQueryById(iamUserId), iamUserId);
        gitlabServiceClientOperator.deleteAdmin(iamUserId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        userAttrService.updateAdmin(iamUserId, Boolean.FALSE);
    }

    @Override
    public String resetGitlabPassword(Long userId) {
        // 校验这个用户是否是自己，目前只允许自己重置自己的gitlab密码
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null || !Objects.equals(Objects.requireNonNull(userId), userDetails.getUserId())) {
            throw new CommonException("error.reset.password.user.not.self");
        }

        // 校验用户是否同步
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        userAttrService.checkUserSync(userAttrDTO, userId);

        // 生成随机密码
        String randomPassword = GenerateUUID.generateRandomGitlabPassword();

        // 更新密码
        gitlabServiceClientOperator.updateUserPassword(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), randomPassword);

        // 返回密码
        return randomPassword;
    }
}
