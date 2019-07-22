package io.choerodon.devops.domain.application.repository;

import java.util.List;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface UserAttrRepository {

    int baseInsert(UserAttrE userAttrE);

    UserAttrE baseQueryById(Long id);

    Long baseQueryUserIdByGitlabUserId(Long gitLabUserId);

    List<UserAttrE> baseListByUserIds(List<Long> userIds);

    UserAttrE baseQueryByGitlabUserId(Long gitlabUserId);

    void baseUpdate(UserAttrE userAttrE);

    List<UserAttrE> baseList();

    UserAttrE baseQueryByGitlabUserName(String gitlabUserName);
}
