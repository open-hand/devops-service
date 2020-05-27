package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsServiceInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2018/4/19.
 */
public interface DevopsServiceInstanceMapper extends BaseMapper<DevopsServiceInstanceDTO> {

    List<DevopsServiceInstanceDTO> listByEnvIdAndInstanceCode(@Param(value = "instanceCode") String instanceCode, @Param(value = "envId") Long envId);

}
