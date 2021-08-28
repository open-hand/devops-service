package io.choerodon.devops.api.vo.deploy;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.market.MarketDeployObjectInfoVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:10
 */
public class JarDeployVO {
    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;

    /**
     * {@link io.choerodon.devops.infra.enums.AppSourceType}
     */
    @ApiModelProperty("部署来源")
    private String sourceType;

    @ApiModelProperty("应用名")
    private String appName;

    @ApiModelProperty("应用编码")
    private String appCode;

    @ApiModelProperty("来源配置")
    private String sourceConfig;

    /**
     * 部署对象id
     */
    private MarketDeployObjectInfoVO marketDeployObjectInfoVO;

    @ApiModelProperty("部署values")
    @NotNull(message = "error.value.is.null")
    private String value;

    private String jarFileUrl;

    private ProdJarInfoVO prodJarInfoVO;

    @ApiModelProperty("部署配置文件列表")
    private List<ConfigSettingVO> configSettingVOS;

    public JarDeployVO() {
        this.value = "";
    }

    public JarDeployVO(String sourceType, String value, ProdJarInfoVO prodJarInfoVO) {
        this.sourceType = sourceType;
        this.value = value;
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public MarketDeployObjectInfoVO getMarketDeployObjectInfoVO() {
        return marketDeployObjectInfoVO;
    }

    public void setMarketDeployObjectInfoVO(MarketDeployObjectInfoVO marketDeployObjectInfoVO) {
        this.marketDeployObjectInfoVO = marketDeployObjectInfoVO;
    }

    public String getJarFileUrl() {
        return jarFileUrl;
    }

    public void setJarFileUrl(String jarFileUrl) {
        this.jarFileUrl = jarFileUrl;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ProdJarInfoVO getProdJarInfoVO() {
        return prodJarInfoVO;
    }

    public void setProdJarInfoVO(ProdJarInfoVO prodJarInfoVO) {
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public List<ConfigSettingVO> getConfigSettingVOS() {
        return configSettingVOS;
    }

    public void setConfigSettingVOS(List<ConfigSettingVO> configSettingVOS) {
        this.configSettingVOS = configSettingVOS;
    }
}
