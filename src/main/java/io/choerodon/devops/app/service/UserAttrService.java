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
     * 如果传入的参数是null，抛出异常
     *
     * @param userAttrDTO 用户信息
     * @param iamUserId   这个对象所对应的iamUserId，抛异常需要
     * @return 通过校验后返回原封不动的入参
     */
    UserAttrDTO checkUserSync(UserAttrDTO userAttrDTO, Long iamUserId);

    /**
     * 根据gitlab用户id查询平台用户id
     *
     * @param gitLabUserId gitLab user id
     * @return user id
     */
    Long queryUserIdByGitlabUserId(Long gitLabUserId);


    UserAttrDTO baseQueryByGitlabUserId(Long gitlabUserId);

    void baseInsert(UserAttrDTO userAttrDTO);

    UserAttrDTO baseQueryById(Long id);

    Long baseQueryUserIdByGitlabUserId(Long gitLabUserId);

    List<UserAttrDTO> baseListByUserIds(List<Long> userIds);

    void baseUpdate(UserAttrDTO userAttrDTO);

    UserAttrDTO baseQueryByGitlabUserName(String gitlabUserName);
}
