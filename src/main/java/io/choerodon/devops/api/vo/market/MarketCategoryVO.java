package io.choerodon.devops.api.vo.market;

import java.util.Date;
import javax.validation.constraints.NotEmpty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by wangxiang on 2020/11/26
 */
public class MarketCategoryVO {
    @Encrypt
    private Long id;

    @NotEmpty
    private String name;
    @NotEmpty
    private String code;
    private Boolean builtIn;

    private Boolean enable;
    private Date lastUpdateDate;
    @Encrypt
    private Long marketAppId;

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getMarketAppId() {
        return marketAppId;
    }

    public void setMarketAppId(Long marketAppId) {
        this.marketAppId = marketAppId;
    }
}
