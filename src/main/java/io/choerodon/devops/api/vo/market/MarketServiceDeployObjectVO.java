package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;


/**
 * Created by wangxiang on 2020/12/17
 */
public class MarketServiceDeployObjectVO {

    private Long id;

    @ApiModelProperty("市场服务id")
    private String marketServiceId;

    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long devopsAppServiceId;

    @ApiModelProperty("应用服务名字")
    private String devopsAppServiceName;

    @ApiModelProperty("应用服务code")
    private String devopsAppServiceCode;

    @ApiModelProperty("devops应用服务版本id")
    private Long devopsAppServiceVersionId;

    @ApiModelProperty("devops应用版本号")
    private String devopsAppServiceVersion;

    @ApiModelProperty("发布jar的来源配置/可为空，如果不为空，则表示发布jar包")
    private String jarSource;

    @ApiModelProperty("存在市场中的源码包的地址/可为空")
    private String marketSourceCodeUrl;

    @ApiModelProperty("存在市场中的镜像的地址/可为空")
    private String marketDockerImageUrl;

    @ApiModelProperty("存在市场中的chart的所属的helm仓库地址/可为空")
    private String marketChartRepository;

    @ApiModelProperty("存在市场中的jar包的地址/可为空")
    private String marketJarLocation;


    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("错误消息")
    private String errorMessage;

    @ApiModelProperty("harbor配置地址")
    @Encrypt
    private Long harborConfigId;
    @ApiModelProperty("chart配置地址")
    @Encrypt
    private Long chartConfigId;
    @ApiModelProperty("maven配置地址")
    @Encrypt
    private Long mavenConfigId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getMarketServiceId() {
        return marketServiceId;
    }

    public void setMarketServiceId(String marketServiceId) {
        this.marketServiceId = marketServiceId;
    }

    public Long getDevopsAppServiceId() {
        return devopsAppServiceId;
    }

    public void setDevopsAppServiceId(Long devopsAppServiceId) {
        this.devopsAppServiceId = devopsAppServiceId;
    }

    public String getDevopsAppServiceName() {
        return devopsAppServiceName;
    }

    public void setDevopsAppServiceName(String devopsAppServiceName) {
        this.devopsAppServiceName = devopsAppServiceName;
    }

    public String getDevopsAppServiceCode() {
        return devopsAppServiceCode;
    }

    public void setDevopsAppServiceCode(String devopsAppServiceCode) {
        this.devopsAppServiceCode = devopsAppServiceCode;
    }

    public Long getDevopsAppServiceVersionId() {
        return devopsAppServiceVersionId;
    }

    public void setDevopsAppServiceVersionId(Long devopsAppServiceVersionId) {
        this.devopsAppServiceVersionId = devopsAppServiceVersionId;
    }

    public String getDevopsAppServiceVersion() {
        return devopsAppServiceVersion;
    }

    public void setDevopsAppServiceVersion(String devopsAppServiceVersion) {
        this.devopsAppServiceVersion = devopsAppServiceVersion;
    }

    public String getJarSource() {
        return jarSource;
    }

    public void setJarSource(String jarSource) {
        this.jarSource = jarSource;
    }

    public String getMarketSourceCodeUrl() {
        return marketSourceCodeUrl;
    }

    public void setMarketSourceCodeUrl(String marketSourceCodeUrl) {
        this.marketSourceCodeUrl = marketSourceCodeUrl;
    }

    public String getMarketDockerImageUrl() {
        return marketDockerImageUrl;
    }

    public void setMarketDockerImageUrl(String marketDockerImageUrl) {
        this.marketDockerImageUrl = marketDockerImageUrl;
    }

    public String getMarketChartRepository() {
        return marketChartRepository;
    }

    public void setMarketChartRepository(String marketChartRepository) {
        this.marketChartRepository = marketChartRepository;
    }

    public String getMarketJarLocation() {
        return marketJarLocation;
    }

    public void setMarketJarLocation(String marketJarLocation) {
        this.marketJarLocation = marketJarLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getHarborConfigId() {
        return harborConfigId;
    }

    public void setHarborConfigId(Long harborConfigId) {
        this.harborConfigId = harborConfigId;
    }

    public Long getChartConfigId() {
        return chartConfigId;
    }

    public void setChartConfigId(Long chartConfigId) {
        this.chartConfigId = chartConfigId;
    }

    public Long getMavenConfigId() {
        return mavenConfigId;
    }

    public void setMavenConfigId(Long mavenConfigId) {
        this.mavenConfigId = mavenConfigId;
    }
}
