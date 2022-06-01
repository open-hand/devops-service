package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/28 17:22
 */
public class MarketDeployObjectInfoVO {
    @Encrypt
    @ApiModelProperty("市场应用版本id")
    private Long mktAppVersionId;
    @Encrypt
    @ApiModelProperty("市场应用部署对象id")
    private Long mktDeployObjectId;

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }

    public Long getMktDeployObjectId() {
        return mktDeployObjectId;
    }

    public void setMktDeployObjectId(Long mktDeployObjectId) {
        this.mktDeployObjectId = mktDeployObjectId;
    }
}
