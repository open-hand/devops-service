package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 15:05
 */
public interface DevopsCiMavenBuildConfigMapper extends BaseMapper<DevopsCiMavenBuildConfigDTO> {

    void batchDeleteByStepIds(@Param("stepIds") Set<Long> stepIds);
}
