package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: Runge
 * Date: 2018/8/3
 * Time: 13:39
 * Description:
 */
public class DevopsServiceTargetVO {
    @ApiModelProperty("选择的实例列表")
    private List<AppServiceInstanceInfoVO> instances;
    @Encrypt
    @ApiModelProperty("目标应用服务id")
    private Long targetAppServiceId;
    @ApiModelProperty("目标应用服务名称")
    private String targetAppServiceName;
    @ApiModelProperty("目标部署组id")
    @Encrypt
    private Long targetDeploymentId;
    @ApiModelProperty("目标部署组名称")
    private String targetDeploymentName;
    /**
     * 是创建网络时所填的标签，也是这个网络本身的选择器
     */
    @ApiModelProperty("网络的选择器")
    private Map<String, String> selectors;
    @ApiModelProperty("目标端点")
    private Map<String, List<EndPointPortVO>> endPoints;

    public List<AppServiceInstanceInfoVO> getInstances() {
        return instances;
    }

    public void setInstances(List<AppServiceInstanceInfoVO> instances) {
        this.instances = instances;

    }

    public Map<String, List<EndPointPortVO>> getEndPoints() {
        return endPoints;
    }

    public Map<String, String> getSelectors() {
        return selectors;
    }

    public void setSelectors(Map<String, String> selectors) {
        this.selectors = selectors;
    }

    public void setEndPoints(Map<String, List<EndPointPortVO>> endPoints) {
        this.endPoints = endPoints;
    }

    public Long getTargetAppServiceId() {
        return targetAppServiceId;
    }

    public void setTargetAppServiceId(Long targetAppServiceId) {
        this.targetAppServiceId = targetAppServiceId;
    }

    public String getTargetAppServiceName() {
        return targetAppServiceName;
    }

    public void setTargetAppServiceName(String targetAppServiceName) {
        this.targetAppServiceName = targetAppServiceName;
    }

    public String getTargetDeploymentName() {
        return targetDeploymentName;
    }

    public void setTargetDeploymentName(String targetDeploymentName) {
        this.targetDeploymentName = targetDeploymentName;
    }

    public Long getTargetDeploymentId() {
        return targetDeploymentId;
    }

    public void setTargetDeploymentId(Long targetDeploymentId) {
        this.targetDeploymentId = targetDeploymentId;
    }
}
