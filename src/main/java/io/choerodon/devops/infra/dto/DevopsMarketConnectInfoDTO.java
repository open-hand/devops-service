package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:21 2019/6/28
 * Description:
 */
@Table(name = "devops_market_connect_info")
public class DevopsMarketConnectInfoDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String saasMarketUrl;
    private String accessToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSaasMarketUrl() {
        return saasMarketUrl;
    }

    public void setSaasMarketUrl(String saasMarketUrl) {
        this.saasMarketUrl = saasMarketUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
