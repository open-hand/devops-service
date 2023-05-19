package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Nginx-Ingress注解配置(IngressNginxAnnotation)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-04-25 15:29:54
 */

@ApiModel("Nginx-Ingress注解配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ingress_nginx_annotation")
public class IngressNginxAnnotationDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_INGRESS_ID = "ingressId";
    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";
    private static final long serialVersionUID = -54366629879997670L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_ingress.id")
    private Long ingressId;

    @ApiModelProperty(value = "key")
    private String annotationKey;

    @ApiModelProperty(value = "value")
    private String annotationValue;

    public IngressNginxAnnotationDTO() {
    }

    public IngressNginxAnnotationDTO(Long ingressId, String annotationKey, String annotationValue) {
        this.ingressId = ingressId;
        this.annotationKey = annotationKey;
        this.annotationValue = annotationValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIngressId() {
        return ingressId;
    }

    public void setIngressId(Long ingressId) {
        this.ingressId = ingressId;
    }

    public String getAnnotationKey() {
        return annotationKey;
    }

    public void setAnnotationKey(String annotationKey) {
        this.annotationKey = annotationKey;
    }

    public String getAnnotationValue() {
        return annotationValue;
    }

    public void setAnnotationValue(String annotationValue) {
        this.annotationValue = annotationValue;
    }

}

