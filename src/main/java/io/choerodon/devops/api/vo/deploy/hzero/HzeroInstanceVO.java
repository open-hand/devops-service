package io.choerodon.devops.api.vo.deploy.hzero;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 9:35
 */
public class HzeroInstanceVO {
    @ApiModelProperty("实例code")
    private String instanceCode;
    @ApiModelProperty("部署配置")
    private String values;
    @ApiModelProperty("市场服务id")
    private Long mktServiceId;
    @ApiModelProperty("部署对象id")
    private Long mktDeployObjectId;

    @ApiModelProperty("部署顺序")
    private Long sequence;

    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;


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

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
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
}
