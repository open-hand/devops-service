package io.choerodon.devops.api.dto;

import java.util.Date;

/**
 * Creator: Runge
 * Date: 2018/5/31
 * Time: 15:48
 * Description:
 */
public class AppMarketVersionDTO {
    private Long id;
    private String version;
    private Boolean isDeployed;
    private Boolean isPublish;
    private Date creationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getDeployed() {
        return isDeployed;
    }

    public void setDeployed(Boolean deployed) {
        isDeployed = deployed;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getPublish() {
        return isPublish;
    }

    public void setPublish(Boolean publish) {
        isPublish = publish;
    }
}
