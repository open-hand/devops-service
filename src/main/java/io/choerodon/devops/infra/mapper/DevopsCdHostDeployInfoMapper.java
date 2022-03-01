package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCdHostDeployInfoDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/14 10:01
 */
public interface DevopsCdHostDeployInfoMapper extends BaseMapper<DevopsCdHostDeployInfoDTO> {
    DevopsCdHostDeployInfoDTO selectByHostAppId(@Param("hostAppId") Long hostAppId);
}
