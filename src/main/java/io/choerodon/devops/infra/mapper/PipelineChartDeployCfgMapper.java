package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.cd.PipelineChartDeployCfgVO;
import io.choerodon.devops.infra.dto.PipelineChartDeployCfgDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * chart部署任务配置表(PipelineChartDeployCfg)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:05
 */
public interface PipelineChartDeployCfgMapper extends BaseMapper<PipelineChartDeployCfgDTO> {

    PipelineChartDeployCfgVO queryVoByConfigId(@Param("configId") Long configId);
}

