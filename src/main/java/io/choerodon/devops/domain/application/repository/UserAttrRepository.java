package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.UserAttrE;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface UserAttrRepository {

    int insert(UserAttrE userAttrE);

    UserAttrE queryById(Long id);

    Long queryUserIdByGitlabUserId(Long gitLabUserId);

    List<UserAttrE> listByUserIds(List<Long> userIds);
}
