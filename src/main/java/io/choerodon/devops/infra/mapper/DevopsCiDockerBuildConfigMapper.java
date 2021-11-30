package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:24
 */
public interface DevopsCiDockerBuildConfigMapper extends BaseMapper<DevopsCiDockerBuildConfigDTO> {

    void batchDeleteByIds(@Param("ids") Set<Long> ids);
}
