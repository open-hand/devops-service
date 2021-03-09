package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by wangxiang on 2021/1/11
 */
public class MarketAppSubscribeRelVO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt
    private Long id;

    @ApiModelProperty("应用id")
    @Encrypt
    private Long marketAppId;

    @ApiModelProperty("用户id")
    @Encrypt
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMarketAppId() {
        return marketAppId;
    }

    public void setMarketAppId(Long marketAppId) {
        this.marketAppId = marketAppId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
