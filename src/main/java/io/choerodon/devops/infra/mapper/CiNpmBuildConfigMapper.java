package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiNpmBuildConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线npm构建配置(CiNpmBuildConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-11 10:42:10
 */
public interface CiNpmBuildConfigMapper extends BaseMapper<CiNpmBuildConfigDTO> {
    void batchDeleteByStepIds(@Param("stepIds") Set<Long> stepIds);
}

