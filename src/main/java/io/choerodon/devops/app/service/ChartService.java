package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.chart.ChartTagVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/26 17:37
 */
public interface ChartService {

    void batchDeleteChartVersion(List<ChartTagVO> chartTagVOS);
}
