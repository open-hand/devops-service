package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 9:59
 */
public interface DevopsHzeroDeployDetailsMapper extends BaseMapper<DevopsHzeroDeployDetailsDTO> {

    List<DevopsHzeroDeployDetailsDTO> listNotSuccessRecordId(@Param("recordId") Long recordId);

    DevopsHzeroDeployDetailsDTO baseQueryDeployingByEnvIdAndInstanceCode(@Param("envId") Long envId,
                                                                         @Param("instanceCode") String instanceCode);
}
