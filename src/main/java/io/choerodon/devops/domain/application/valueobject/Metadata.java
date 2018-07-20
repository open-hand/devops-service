package io.choerodon.devops.domain.application.valueobject;

import java.util.Date;

public class Metadata {
    private Date creationTimestamp;

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
