package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiNpmPublishConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线npm发布配置(CiNpmPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 15:26:47
 */
public interface CiNpmPublishConfigMapper extends BaseMapper<CiNpmPublishConfigDTO> {
    void batchDeleteByStepIds(@Param("stepIds") Set<Long> stepIds);
}

