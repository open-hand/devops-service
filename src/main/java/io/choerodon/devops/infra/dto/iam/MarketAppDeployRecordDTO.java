package io.choerodon.devops.infra.dto.iam;

import java.util.Date;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  13:57 2019/7/25
 * Description:
 */
public class MarketAppDeployRecordDTO {
    private Long id;
    @NotNull(message = "error.market.app.deploy.record.create.app.id.cannot.be.null")
    private Long appId;
    @ApiModelProperty("组织Id")
    private Long organizationId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("部署日期")
    private Date deployDate;
    @NotNull(message = "error.market.app.deploy.record.create.version.id.cannot.be.null")
    @ApiModelProperty("部署应用的版本的ID")
    private Long versionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Date getDeployDate() {
        return deployDate;
    }

    public void setDeployDate(Date deployDate) {
        this.deployDate = deployDate;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
