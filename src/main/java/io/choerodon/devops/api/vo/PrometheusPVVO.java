package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author: 25499
 * @date: 2019/11/8 12:02
 * @description:
 */
public class PrometheusPVVO {
    @ApiModelProperty("PVC的类型,根据对应类型的分别选填：prometheus-pvc/alertmanager-pvc/grafana-pvc")
    private String type;
    @ApiModelProperty("PVC的ID")
    private Long pvId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getPvId() {
        return pvId;
    }

    public void setPvId(Long pvId) {
        this.pvId = pvId;
    }
}
