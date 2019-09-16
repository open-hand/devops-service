package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by younger on 2018/4/10.
 */
public class AppServiceUpdateDTO {

    @ApiModelProperty("id")
    @NotNull(message = "error.id.null")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("图片链接")
    private String imgUrl;
    @ApiModelProperty("harbor配置")
    private DevopsConfigVO harbor;
    @ApiModelProperty("char配置")
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
