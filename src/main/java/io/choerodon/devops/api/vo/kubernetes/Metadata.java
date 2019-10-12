package io.choerodon.devops.api.vo.kubernetes;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class Metadata {
    @ApiModelProperty("创建时间")
    private Date creationTimestamp;
    @ApiModelProperty("名称")
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
