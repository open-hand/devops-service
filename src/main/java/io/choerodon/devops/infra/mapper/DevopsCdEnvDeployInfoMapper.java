package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCdEnvDeployInfoDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/8 15:30
 */
public interface DevopsCdEnvDeployInfoMapper extends BaseMapper<DevopsCdEnvDeployInfoDTO> {

    List<DevopsCdEnvDeployInfoDTO> queryCurrentByValueId(@Param("valueId") Long valueId);

    List<DevopsCdEnvDeployInfoDTO> queryCurrentByEnvId(@Param("envId") Long envId);
}
