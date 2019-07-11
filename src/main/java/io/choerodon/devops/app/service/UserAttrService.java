package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.UserAttrDTO;

public interface UserAttrService {

    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    UserAttrDTO  queryByUserId(Long userId);

}
