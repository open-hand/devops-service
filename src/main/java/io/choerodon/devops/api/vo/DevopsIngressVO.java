package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:56
 * Description:
 */
public class DevopsIngressVO extends DevopsResourceDataInfoVO {
    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty("关联的应用服务id")
    private Long appServiceId;
    @ApiModelProperty("域名")
    private String domain;
    @ApiModelProperty("域名名称")
    private String name;
    @Encrypt
    @ApiModelProperty("关联的环境id")
    private Long envId;
    @ApiModelProperty("关联的环境名称")
    private String envName;
    @ApiModelProperty("关联的环境状态")
    private Boolean envStatus;
    private Boolean isUsable;
    @ApiModelProperty("网络的状态")
    private String status;
    @Encrypt
    @ApiModelProperty("关联的证书id")
    private Long certId;
    @ApiModelProperty("关联的证书名称")
    private String certName;
    @ApiModelProperty("关联的证书状态")
    private String certStatus;
    @ApiModelProperty("域名对应的path，其中是path对象")
    private List<DevopsIngressPathVO> pathList;
    @ApiModelProperty("command操作类型")
    private String commandType;
    @ApiModelProperty("command操作状态")
    private String commandStatus;
    @ApiModelProperty("错误信息")
    private String error;
    @ApiModelProperty("Annotations键值对，键不是确定值")
    private Map<String, String> annotations;
    @ApiModelProperty("实例名称数组")
    private List<String> instances;
    @ApiModelProperty("如果ingress由helm实例产生，此字段为helm实例id")
    private Long instanceId;

    private List<NginxIngressAnnotationVO> nginxIngressAnnotations;

    public List<NginxIngressAnnotationVO> getNginxIngressAnnotations() {
        return nginxIngressAnnotations;
    }

    public void setNginxIngressAnnotations(List<NginxIngressAnnotationVO> nginxIngressAnnotations) {
        this.nginxIngressAnnotations = nginxIngressAnnotations;
    }

    public DevopsIngressVO() {
        this.pathList = new ArrayList<>();
    }

    /**
     * 构造函数
     */
    public DevopsIngressVO(Long id, String domain, String name,
                           Long envId, Boolean isUsable, String envName, Long instanceId) {
        this.envId = envId;
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.pathList = new ArrayList<>();
        this.isUsable = isUsable;
        this.envName = envName;
        this.instanceId = instanceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public List<DevopsIngressPathVO> getPathList() {
        return pathList;
    }

    public void setPathList(List<DevopsIngressPathVO> pathList) {
        this.pathList = pathList;
    }

    public DevopsIngressPathVO queryLastDevopsIngressPathDTO() {
        Integer size = pathList.size();
        return size == 0 ? null : pathList.get(size - 1);
    }

    public void addDevopsIngressPathDTO(DevopsIngressPathVO devopsIngressPathVO) {
        this.pathList.add(devopsIngressPathVO);
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

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Boolean getUsable() {
        return isUsable;
    }

    public void setUsable(Boolean usable) {
        isUsable = usable;
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

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
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

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public List<String> getInstances() {
        return instances;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevopsIngressVO that = (DevopsIngressVO) o;
        return Objects.equals(domain, that.domain)
                && Objects.equals(name, that.name)
                && Objects.equals(envId, that.envId)
                && Objects.equals(pathList, that.pathList)
                && Objects.equals(certId, that.certId);
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, name, envId, pathList);
    }

    @Override
    public String toString() {
        return "DevopsIngressVO{" +
                "id=" + id +
                ", appServiceId=" + appServiceId +
                ", domain='" + domain + '\'' +
                ", name='" + name + '\'' +
                ", envId=" + envId +
                ", envName='" + envName + '\'' +
                ", envStatus=" + envStatus +
                ", isUsable=" + isUsable +
                ", status='" + status + '\'' +
                ", certId=" + certId +
                ", certName='" + certName + '\'' +
                ", certStatus='" + certStatus + '\'' +
                ", pathList=" + pathList +
                ", commandType='" + commandType + '\'' +
                ", commandStatus='" + commandStatus + '\'' +
                ", error='" + error + '\'' +
                ", annotations=" + annotations +
                ", instances=" + instances +
                ", instanceId=" + instanceId +
                '}';
    }
}
