package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.infra.dto.UserAttrDTO;

public interface UserAttrService {

    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    UserAttrVO queryByUserId(Long userId);

    /**
     * 根据gitlab用户id查询平台用户id
     *
     * @param gitLabUserId gitLab user id
     * @return user id
     */
    Long queryUserIdByGitlabUserId(Long gitLabUserId);


    UserAttrDTO baseQueryByGitlabUserId(Long gitlabUserId);

    int baseInsert(UserAttrDTO userAttrDTO);

    UserAttrDTO baseQueryById(Long id);

    Long baseQueryUserIdByGitlabUserId(Long gitLabUserId);

    List<UserAttrDTO> baseListByUserIds(List<Long> userIds);

    void baseUpdate(UserAttrDTO userAttrDTO);

    UserAttrDTO baseQueryByGitlabUserName(String gitlabUserName);
}
