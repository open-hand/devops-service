package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.UserAttrE;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface UserAttrRepository {

    int insert(UserAttrE userAttrE);

    UserAttrE queryById(Long id);

    Long queryUserIdByGitlabUserId(Long gitLabUserId);
}
