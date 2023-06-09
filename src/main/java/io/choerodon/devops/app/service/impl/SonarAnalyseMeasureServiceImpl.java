package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.SonarAnalyseMeasureService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.SonarAnalyseMeasureDTO;
import io.choerodon.devops.infra.mapper.SonarAnalyseMeasureMapper;

/**
 * 代码扫描指标详情表(SonarAnalyseMeasure)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:56
 */
@Service
public class SonarAnalyseMeasureServiceImpl implements SonarAnalyseMeasureService {
    @Autowired
    private SonarAnalyseMeasureMapper sonarAnalyseMeasureMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long recordId, List<SonarAnalyseMeasureDTO> sonarAnalyseMeasureDTOS) {
        sonarAnalyseMeasureMapper.batchSave(recordId, sonarAnalyseMeasureDTOS);
    }

    @Override
    public List<SonarAnalyseMeasureDTO> listByRecordId(Long recordId) {
        Assert.notNull(recordId, ResourceCheckConstant.DEVOPS_RECORD_ID_IS_NULL);

        SonarAnalyseMeasureDTO record = new SonarAnalyseMeasureDTO();
        record.setRecordId(recordId);
        return sonarAnalyseMeasureMapper.select(record);
    }
}

