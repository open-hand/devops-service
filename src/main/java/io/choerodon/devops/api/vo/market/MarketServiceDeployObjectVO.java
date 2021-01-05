package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import javax.annotation.Nullable;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class MarketServiceDeployObjectVO {

    private Long id;

    @ApiModelProperty("市场服务id")
    private Long marketServiceId;

    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long devopsAppServiceId;

    @ApiModelProperty("应用服务名字")
    private String devopsAppServiceName;

    @ApiModelProperty("应用服务code")
    private String devopsAppServiceCode;

    /**
     * 形如: devops-service-129089145980129
     */
    @ApiModelProperty("市场的制品code/有发布部署包时才有值")
    private String marketArtifactCode;

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

    private MarketChartConfigVO marketChartConfigVO;

    private MarketHarborConfigVO marketHarborConfigVO;

    private MarketMavenConfigVO marketMavenConfigVO;

    /**
     * 市场应用的名称
     */
    private String marketAppName;
    /**
     * 应用市场版本
     */
    private String marketAppVersion;

    /**
     * 市场服务名称
     */
    private String marketServiceName;

    /**
     * 市场服务版本
     */
    private String marketServiceVersion;

    @Nullable
    @ApiModelProperty("版本的values")
    private String value;

    public String getMarketAppVersion() {
        return marketAppVersion;
    }

    public void setMarketAppVersion(String marketAppVersion) {
        this.marketAppVersion = marketAppVersion;
    }

    public String getMarketServiceVersion() {
        return marketServiceVersion;
    }

    public void setMarketServiceVersion(String marketServiceVersion) {
        this.marketServiceVersion = marketServiceVersion;
    }

    public String getMarketAppName() {
        return marketAppName;
    }

    public void setMarketAppName(String marketAppName) {
        this.marketAppName = marketAppName;
    }

    public String getMarketServiceName() {
        return marketServiceName;
    }

    public void setMarketServiceName(String marketServiceName) {
        this.marketServiceName = marketServiceName;
    }

    public MarketChartConfigVO getMarketChartConfigVO() {
        return marketChartConfigVO;
    }

    public void setMarketChartConfigVO(MarketChartConfigVO marketChartConfigVO) {
        this.marketChartConfigVO = marketChartConfigVO;
    }

    public MarketHarborConfigVO getMarketHarborConfigVO() {
        return marketHarborConfigVO;
    }

    public void setMarketHarborConfigVO(MarketHarborConfigVO marketHarborConfigVO) {
        this.marketHarborConfigVO = marketHarborConfigVO;
    }

    public MarketMavenConfigVO getMarketMavenConfigVO() {
        return marketMavenConfigVO;
    }

    public void setMarketMavenConfigVO(MarketMavenConfigVO marketMavenConfigVO) {
        this.marketMavenConfigVO = marketMavenConfigVO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMarketServiceId() {
        return marketServiceId;
    }

    public void setMarketServiceId(Long marketServiceId) {
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

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    public String getMarketArtifactCode() {
        return marketArtifactCode;
    }

    public void setMarketArtifactCode(String marketArtifactCode) {
        this.marketArtifactCode = marketArtifactCode;
    }
}
