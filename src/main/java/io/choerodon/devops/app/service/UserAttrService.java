package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.UserAttrVO;

public interface UserAttrService {

    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    UserAttrVO queryByUserId(Long userId);

    /**
     *
     * @return
     */
    Integer getGitlabUserId();

    /**
     *
     * @param gitLabUserId
     * @return
     */
    Long getUserIdByGitlabUserId(Long gitLabUserId);
}
