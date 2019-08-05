package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * Created by younger on 2018/4/14.
 */
public class AppServiceVersionVO {
    private Long id;
    private String version;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
