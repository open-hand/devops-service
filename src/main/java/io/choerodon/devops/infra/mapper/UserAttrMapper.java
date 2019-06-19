package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dataobject.UserAttrDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by zzy on 2018/3/26.
 */
public interface UserAttrMapper extends Mapper<UserAttrDO> {
    List<UserAttrDO> listByUserIds(@Param("userIds") List<Long> userIds);
}
