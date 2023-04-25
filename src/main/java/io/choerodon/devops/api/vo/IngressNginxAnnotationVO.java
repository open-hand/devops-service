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
    private String annotationKey;

    private String annotationValue;

    private String type;


    public IngressNginxAnnotationVO(String annotationKey, String type) {
        this.annotationKey = annotationKey;
        this.type = type;
    }

    public Long getIngressId() {
        return ingressId;
    }

    public void setIngressId(Long ingressId) {
        this.ingressId = ingressId;
    }

    public String getAnnotationValue() {
        return annotationValue;
    }

    public void setAnnotationValue(String annotationValue) {
        this.annotationValue = annotationValue;
    }

    public String getAnnotationKey() {
        return annotationKey;
    }

    public void setAnnotationKey(String annotationKey) {
        this.annotationKey = annotationKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
