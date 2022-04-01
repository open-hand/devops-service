package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsCdHostDeployInfoDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/14 10:01
 */
public interface DevopsCdHostDeployInfoMapper extends BaseMapper<DevopsCdHostDeployInfoDTO> {
    List<DevopsCdHostDeployInfoDTO> selectByHostAppId(@Param("hostAppId") Long hostAppId);
}
