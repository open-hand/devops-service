package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.SonarAnalyseMeasureDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 代码扫描指标详情表(SonarAnalyseMeasure)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:55
 */
public interface SonarAnalyseMeasureMapper extends BaseMapper<SonarAnalyseMeasureDTO> {
    void batchSave(@Param("recordId") Long recordId,
                   @Param("sonarAnalyseMeasureDTOS") List<SonarAnalyseMeasureDTO> sonarAnalyseMeasureDTOS);

    List<SonarAnalyseMeasureDTO> listAppLatestMeasures(@Param("projectId") Long projectId,
                                                       @Param("metricTypes") List<String> metricTypes);
}

