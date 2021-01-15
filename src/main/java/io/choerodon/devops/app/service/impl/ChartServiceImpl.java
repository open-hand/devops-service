package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.chart.ChartTagVO;
import io.choerodon.devops.app.service.ChartService;
import io.choerodon.devops.infra.util.ChartUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/26 17:37
 */
@Service
public class ChartServiceImpl implements ChartService {
    @Autowired
    private ChartUtil chartUtil;

    @Override
    public void batchDeleteChartVersion(List<ChartTagVO> chartTagVOS) {
        chartTagVOS.forEach(chartUtil::deleteChart);
    }
}
