package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 资源视图下环境目录信息
 *
 * @author zmf
 */
public class DevopsResourceEnvOverviewVO {
    @Encrypt
    @ApiModelProperty("环境id")
    private Long id;

    @ApiModelProperty("环境名称")
    private String name;

    @ApiModelProperty("环境编码")
    private String code;

    @ApiModelProperty("环境是否连接")
    private Boolean connect;

    @ApiModelProperty("实例基本信息")
    private List<DevopsAppServiceInstanceViewVO> instances;

    @ApiModelProperty("网络基本信息")
    private List<DevopsResourceBasicInfoVO> services;

    @ApiModelProperty("域名基本信息")
    private List<DevopsResourceBasicInfoVO> ingresses;

    @ApiModelProperty("证书基本信息")
    private List<DevopsResourceBasicInfoVO> certifications;

    @ApiModelProperty("配置映射基本信息")
    private List<DevopsResourceBasicInfoVO> configMaps;

    @ApiModelProperty("密文基本信息")
    private List<DevopsResourceBasicInfoVO> secrets;

    @ApiModelProperty("自定义资源基本信息")
    private List<DevopsResourceBasicInfoVO> customResources;

    @ApiModelProperty("PVC基本信息")
    private List<DevopsResourceBasicInfoVO> pvcs;

    @ApiModelProperty("是否开启确认副本生效策略，默认为false")
    private Boolean checkValuesPolicy;

    public Boolean getCheckValuesPolicy() {
        return checkValuesPolicy;
    }

    public void setCheckValuesPolicy(Boolean checkValuesPolicy) {
        this.checkValuesPolicy = checkValuesPolicy;
    }

    public List<DevopsResourceBasicInfoVO> getPvcs() {
        return pvcs;
    }

    public void setPvcs(List<DevopsResourceBasicInfoVO> pvcs) {
        this.pvcs = pvcs;
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

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public List<DevopsAppServiceInstanceViewVO> getInstances() {
        return instances;
    }

    public void setInstances(List<DevopsAppServiceInstanceViewVO> instances) {
        this.instances = instances;
    }

    public List<DevopsResourceBasicInfoVO> getServices() {
        return services;
    }

    public void setServices(List<DevopsResourceBasicInfoVO> services) {
        this.services = services;
    }

    public List<DevopsResourceBasicInfoVO> getIngresses() {
        return ingresses;
    }

    public void setIngresses(List<DevopsResourceBasicInfoVO> ingresses) {
        this.ingresses = ingresses;
    }

    public List<DevopsResourceBasicInfoVO> getConfigMaps() {
        return configMaps;
    }

    public void setConfigMaps(List<DevopsResourceBasicInfoVO> configMaps) {
        this.configMaps = configMaps;
    }

    public List<DevopsResourceBasicInfoVO> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<DevopsResourceBasicInfoVO> secrets) {
        this.secrets = secrets;
    }

    public List<DevopsResourceBasicInfoVO> getCustomResources() {
        return customResources;
    }

    public void setCustomResources(List<DevopsResourceBasicInfoVO> customResources) {
        this.customResources = customResources;
    }

    public List<DevopsResourceBasicInfoVO> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<DevopsResourceBasicInfoVO> certifications) {
        this.certifications = certifications;
    }

    public String getCode() {
        return code;
    }

    public DevopsResourceEnvOverviewVO setCode(String code) {
        this.code = code;
        return this;
    }
}
