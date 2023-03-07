package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface UserAttrService {

    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    UserAttrVO queryByUserId(Long userId);

    /**
     * 根据多个iamUserId查询用户信息
     *
     * @param userIds 用户id
     * @return 存在的用户信息
     */
    List<UserAttrVO> listByUserIds(Set<Long> userIds);

    /**
     * 根据多个gitlabUserId查询用户信息
     *
     * @param gitlabUserIds gitlab用户id
     * @return 没查出的也给出对应的纪录
     */
    List<UserAttrVO> listUsersByGitlabUserIds(Set<Long> gitlabUserIds);

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

    List<UserAttrDTO> baseListByGitlabUserIds(List<Long> gitlabUserIds);

    List<UserAttrDTO> baseListByUserIds(List<Long> userIds);

    void baseUpdate(UserAttrDTO userAttrDTO);

    UserAttrDTO baseQueryByGitlabUserName(String gitlabUserName);

    Long getIamUserIdByGitlabUserName(String username);

    /**
     * 更改用户 is_gitlab_admin字段的值
     *
     * @param iamUserId     iam用户id
     * @param isGitlabAdmin 是否是gitlab管理员
     */
    void updateAdmin(Long iamUserId, Boolean isGitlabAdmin);

    void updateAdmins(List<Long> iamUserIds, Boolean isGitlabAdmin);

    Page<IamUserDTO> queryByAppServiceId(Long projectId, Long appServiceId, PageRequest pageRequest, String params);

    /**
     * 所有 devops_user 纪录的数量
     *
     * @return 数量
     */
    int allUserCount();

    /**
     * 所有 devops_user 纪录的 iam_user_id
     *
     * @return id
     */
    Set<Long> allUserIds();


    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    UserAttrDTO queryGitlabAdminByIamId();

    /**
     * 查询猪齿鱼中的所有gitlabAdmin用户
     *
     * @return
     */
    List<UserAttrVO> listAllAdmin();

    void updateGitlabAdminUserToNormalUser(List<Long> iamUserIds);

    /**
     * 查询用户的模拟令牌，不存在则创建
     *
     * @param iamUserId
     * @return
     */
    String queryOrCreateImpersonationToken(Long iamUserId);
}
