package io.choerodon.devops.api.vo.kubernetes;

import java.util.Date;

public class Metadata {
    private Date creationTimestamp;
    private String name;


    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
