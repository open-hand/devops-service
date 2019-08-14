package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Created by younger on 2018/4/10.
 */
public class AppServiceUpdateDTO {

    private Long id;
    private String name;
    private DevopsConfigVO harbor;
    private DevopsConfigVO chart;

    public DevopsConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(DevopsConfigVO harbor) {
        this.harbor = harbor;
    }

    public DevopsConfigVO getChart() {
        return chart;
    }

    public void setChart(DevopsConfigVO chart) {
        this.chart = chart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
