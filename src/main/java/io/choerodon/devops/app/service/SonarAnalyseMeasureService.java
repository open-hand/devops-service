package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.SonarAnalyseMeasureDTO;

/**
 * 代码扫描指标详情表(SonarAnalyseMeasure)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:55
 */
public interface SonarAnalyseMeasureService {

    void batchSave(Long recordId, List<SonarAnalyseMeasureDTO> sonarAnalyseMeasureDTOS);

}

