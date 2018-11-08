package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsServiceAppInstanceDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2018/4/19.
 */
public interface DevopsServiceAppInstanceMapper extends BaseMapper<DevopsServiceAppInstanceDO> {
    void deleteByServiceIds(@Param("serviceIds") List<Long> serviceIds);
}
