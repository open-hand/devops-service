package io.choerodon.devops.infra.dto.market;

/**
 * @author zmf
 * @since 2020/12/14
 */
public class MarketServiceVersionDTO {
    private Long id;

    private Long marketAppServiceId;

    private Long devopsAppServiceId;

    private Long devopsAppServiceVersionId;

    private String devopsAppServiceCode;

    private String chartVersion;

    private String chartRepo;

    private String serviceName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDevopsAppServiceId() {
        return devopsAppServiceId;
    }

    public void setDevopsAppServiceId(Long devopsAppServiceId) {
        this.devopsAppServiceId = devopsAppServiceId;
    }

    public Long getDevopsAppServiceVersionId() {
        return devopsAppServiceVersionId;
    }

    public void setDevopsAppServiceVersionId(Long devopsAppServiceVersionId) {
        this.devopsAppServiceVersionId = devopsAppServiceVersionId;
    }

    public String getDevopsAppServiceCode() {
        return devopsAppServiceCode;
    }

    public void setDevopsAppServiceCode(String devopsAppServiceCode) {
        this.devopsAppServiceCode = devopsAppServiceCode;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getChartRepo() {
        return chartRepo;
    }

    public void setChartRepo(String chartRepo) {
        this.chartRepo = chartRepo;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getMarketAppServiceId() {
        return marketAppServiceId;
    }

    public void setMarketAppServiceId(Long marketAppServiceId) {
        this.marketAppServiceId = marketAppServiceId;
    }

    // TODO
}
