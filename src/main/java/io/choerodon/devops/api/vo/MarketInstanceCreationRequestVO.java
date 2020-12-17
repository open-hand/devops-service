package io.choerodon.devops.api.vo;

import javax.annotation.Nullable;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 * @since 2020/12/14
 */
public class MarketInstanceCreationRequestVO {
    @Nullable
    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @Encrypt
    @ApiModelProperty("市场市场服务版本id")
    private Long marketAppServiceVersionId;

    @ApiModelProperty("values内容")
    private String values;

    @ApiModelProperty("实例的code")
    private String instanceName;

    /**
     * {@link io.choerodon.devops.infra.enums.CommandType}
     */
    @ApiModelProperty("操作类型")
    private String commandType;

    @Encrypt
    @ApiModelProperty("环境id")
    private Long environmentId;

    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;

    @Nullable
    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(@Nullable Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getMarketAppServiceVersionId() {
        return marketAppServiceVersionId;
    }

    public void setMarketAppServiceVersionId(Long marketAppServiceVersionId) {
        this.marketAppServiceVersionId = marketAppServiceVersionId;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
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
}
