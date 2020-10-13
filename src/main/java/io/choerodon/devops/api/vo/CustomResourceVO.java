package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.api.vo.chart.ChartTagVO;
import io.choerodon.devops.infra.dto.harbor.HarborImageTagDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/26 16:52
 */
public class CustomResourceVO {
    private List<HarborImageTagDTO> harborImageTagDTOS;
    private List<ChartTagVO> chartTagVOS;

    public List<HarborImageTagDTO> getHarborImageTagDTOS() {
        return harborImageTagDTOS;
    }

    public void setHarborImageTagDTOS(List<HarborImageTagDTO> harborImageTagDTOS) {
        this.harborImageTagDTOS = harborImageTagDTOS;
    }

    public List<ChartTagVO> getChartTagVOS() {
        return chartTagVOS;
    }

    public void setChartTagVOS(List<ChartTagVO> chartTagVOS) {
        this.chartTagVOS = chartTagVOS;
    }
}
