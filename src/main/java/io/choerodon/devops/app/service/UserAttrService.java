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
     * @return
     */
    Integer getGitlabUserId();


    /**
     * @param gitLabUserId
     * @return
     */
    Long getUserIdByGitlabUserId(Long gitLabUserId);


    UserAttrDTO baseQueryByGitlabUserId(Long gitlabUserId);

    int baseInsert(UserAttrDTO userAttrDTO);

    UserAttrDTO baseQueryById(Long id);

    Long baseQueryUserIdByGitlabUserId(Long gitLabUserId);

    List<UserAttrDTO> baseListByUserIds(List<Long> userIds);

    UserAttrDTO baseQueryByGitlabUserId(Long gitlabUserId);

    void baseUpdate(UserAttrDTO userAttrDTO);

    List<UserAttrDTO> baseList();

    UserAttrDTO baseQueryByGitlabUserName(String gitlabUserName);
}
