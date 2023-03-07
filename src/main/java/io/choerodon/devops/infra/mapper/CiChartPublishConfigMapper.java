package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiChartPublishConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线chart发布配置(CiChartPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-04 15:28:30
 */
public interface CiChartPublishConfigMapper extends BaseMapper<CiChartPublishConfigDTO> {
    void batchDeleteByStepIds(@Param("stepIds") Set<Long> stepIds);
}

