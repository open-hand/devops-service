package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.UserAttrDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by zzy on 2018/3/26.
 */
public interface UserAttrMapper extends BaseMapper<UserAttrDO> {
    List<UserAttrDO> listByUserIds(@Param("userIds") List<Long> userIds);
}
