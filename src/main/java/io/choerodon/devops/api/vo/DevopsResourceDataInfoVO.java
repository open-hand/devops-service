package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * 资源数据的数据库纪录元信息
 * @author zmf
 */
public class DevopsResourceDataInfoVO {
    private Date lastUpdateDate;
    private Date creationDate;
    private String creatorName;

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
}
