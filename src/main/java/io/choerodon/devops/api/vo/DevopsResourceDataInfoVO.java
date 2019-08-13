package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * 资源数据的数据库纪录元信息
 *
 * @author zmf
 */
public class DevopsResourceDataInfoVO {
    @ApiModelProperty("上次更新时间")
    private Date lastUpdateDate;
    @ApiModelProperty("创建时间")
    private Date creationDate;
    @ApiModelProperty("创建者/为空时是GitOps逻辑创建的")
    private String creatorName;
    @ApiModelProperty("更新者/为空可能是GitOps逻辑创建的也可能是没有更新过")
    private String lastUpdaterName;

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getLastUpdaterName() {
        return lastUpdaterName;
    }

    public void setLastUpdaterName(String lastUpdaterName) {
        this.lastUpdaterName = lastUpdaterName;
    }
}
