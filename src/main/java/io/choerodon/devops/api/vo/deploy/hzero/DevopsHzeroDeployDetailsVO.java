package io.choerodon.devops.api.vo.deploy.hzero;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;

import java.util.Date;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 9:35
 */
public class DevopsHzeroDeployDetailsVO {

    private Long id;
    @ApiModelProperty("实例code")
    private String instanceCode;
    @ApiModelProperty("部署配置id")
    @Encrypt
    private Long valueId;
    @ApiModelProperty("部署配置")
    private String value;
    @ApiModelProperty("市场服务id")
    private Long mktServiceId;
    @ApiModelProperty("部署对象id")
    private Long mktDeployObjectId;

    @ApiModelProperty("部署顺序")
    private Long sequence;

    /**
     * {@link io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum}
     */
    @ApiModelProperty("部署状态")
    private String status;

    @ApiModelProperty("开始部署时间")
    private Date startTime;

    @ApiModelProperty("结束部署时间")
    private Date endTime;

    private String mktServiceName;

    private String mktServiceVersion;

    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getMktServiceName() {
        return mktServiceName;
    }

    public void setMktServiceName(String mktServiceName) {
        this.mktServiceName = mktServiceName;
    }

    public String getMktServiceVersion() {
        return mktServiceVersion;
    }

    public void setMktServiceVersion(String mktServiceVersion) {
        this.mktServiceVersion = mktServiceVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public DevopsServiceReqVO getDevopsServiceReqVO() {
        return devopsServiceReqVO;
    }

    public void setDevopsServiceReqVO(DevopsServiceReqVO devopsServiceReqVO) {
        this.devopsServiceReqVO = devopsServiceReqVO;
    }

    public DevopsIngressVO getDevopsIngressVO() {
        return devopsIngressVO;
    }

    public void setDevopsIngressVO(DevopsIngressVO devopsIngressVO) {
        this.devopsIngressVO = devopsIngressVO;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getMktServiceId() {
        return mktServiceId;
    }

    public void setMktServiceId(Long mktServiceId) {
        this.mktServiceId = mktServiceId;
    }

    public Long getMktDeployObjectId() {
        return mktDeployObjectId;
    }

    public void setMktDeployObjectId(Long mktDeployObjectId) {
        this.mktDeployObjectId = mktDeployObjectId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
