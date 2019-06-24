package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dataobject.DevopsServiceAppInstanceDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Zenger on 2018/4/19.
 */
public interface DevopsServiceAppInstanceMapper extends Mapper<DevopsServiceAppInstanceDO> {

    List<DevopsServiceAppInstanceDO> listByEnvIdAndInstanceCode(@Param(value = "instanceCode") String instanceCode, @Param(value = "envId") Long envId);

}
