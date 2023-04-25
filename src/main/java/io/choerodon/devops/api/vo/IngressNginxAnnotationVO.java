package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/4/25 14:50
 */
public class IngressNginxAnnotationVO {

    @ApiModelProperty(value = "devops_ingress.id")
    private Long ingressId;
    private String key;

    private String value;

    private String type;


    public IngressNginxAnnotationVO(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public Long getIngressId() {
        return ingressId;
    }

    public void setIngressId(Long ingressId) {
        this.ingressId = ingressId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
