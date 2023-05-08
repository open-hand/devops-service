package io.choerodon.devops.infra.dto;

import java.util.List;
import java.util.Objects;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.IngressNginxAnnotationVO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ingress")
public class DevopsIngressDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_ingress";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Encrypt(DevopsIngressDTO.ENCRYPT_KEY)
    private Long id;
    private Long projectId;
    private Long envId;
    private Long commandId;
    private String name;
    private String domain;
    @ApiModelProperty("ingress对象的Annotations字段的JSON格式字符串")
    private String annotations;
    private Boolean isUsable;
    private String status;
    private Long certId;
    @ApiModelProperty("如果ingress由helm实例产生，此字段为helm实例id")
    private Long instanceId;

    @Transient
    private String envName;
    @Transient
    private String namespace;
    @Transient
    private List<DevopsIngressPathDTO> devopsIngressPathDTOS;

    @Transient
    private List<IngressNginxAnnotationVO> nginxIngressAnnotations;
    @Transient
    private String commandType;
    @Transient
    private String commandStatus;
    @Transient
    private String error;
    @Transient
    private String message;

    public DevopsIngressDTO() {
    }

    public DevopsIngressDTO(String name) {
        this.name = name;
    }

    /**
     * 构造函数
     */
    public DevopsIngressDTO(Long projectId, Long envId, String domain, String name) {
        this.projectId = projectId;
        this.envId = envId;
        this.domain = domain;
        this.name = name;
    }

    public List<IngressNginxAnnotationVO> getNginxIngressAnnotations() {
        return nginxIngressAnnotations;
    }

    public void setNginxIngressAnnotations(List<IngressNginxAnnotationVO> nginxIngressAnnotations) {
        this.nginxIngressAnnotations = nginxIngressAnnotations;
    }

    public DevopsIngressDTO(Long projectId) {
        this.projectId = projectId;
    }

    public DevopsIngressDTO(Long projectId, String name) {
        this.projectId = projectId;
        this.name = name;
    }

    public DevopsIngressDTO(String domain, Long projectId) {
        this.projectId = projectId;
        this.domain = domain;
    }


    /**
     * 构造函数
     */
    public DevopsIngressDTO(Long id, Long projectId, Long envId, String domain, String name, String status) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.envId = envId;
        this.domain = domain;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUsable() {
        return isUsable;
    }

    public void setUsable(Boolean usable) {
        isUsable = usable;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public List<DevopsIngressPathDTO> getDevopsIngressPathDTOS() {
        return devopsIngressPathDTOS;
    }

    public void setDevopsIngressPathDTOS(List<DevopsIngressPathDTO> devopsIngressPathDTOS) {
        this.devopsIngressPathDTOS = devopsIngressPathDTOS;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevopsIngressDTO that = (DevopsIngressDTO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(envId, that.envId)
                && Objects.equals(name, that.name)
                && Objects.equals(status, that.status)
                && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, envId, name, domain);
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
}
