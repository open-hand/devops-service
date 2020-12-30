package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * Created by zzy on 2018/3/26.
 */
public interface UserAttrMapper extends BaseMapper<UserAttrDTO> {
    List<UserAttrDTO> listByUserIds(@Param("userIds") List<Long> userIds);

    List<UserAttrDTO> listByGitlabUserIds(@Param("gitlabUserIds") List<Long> gitlabUserIds);

    void updateIsGitlabAdmin(@Param("iamUserId") Long iamUserId,
                             @Param("isGitlabAdmin") Boolean isGitlabAdmin);

    Set<Long> selectAllUserIds();
}
