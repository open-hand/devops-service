package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by wangxiang on 2021/3/4
 */
public class DevopsPipelineRecordVO {

    @Encrypt
    private Long id;

    @ApiModelProperty("流水线状态")
    private String status;

    @ApiModelProperty("创建时间")
    private Date createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
