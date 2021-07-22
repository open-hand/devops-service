package io.choerodon.devops.api.vo.host;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:22
 */
public class DevopsNormalInstanceVO extends DevopsHostInstanceVO {

    @ApiModelProperty("进程id")
    private String pid;
    @ApiModelProperty("占用端口")
    private String port;
    @ApiModelProperty("部署来源")
    private String sourceType;

    private IamUserDTO deployer;

    private Long createdBy;

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public IamUserDTO getDeployer() {
        return deployer;
    }

    public void setDeployer(IamUserDTO deployer) {
        this.deployer = deployer;
    }

    private DevopsHostCommandDTO devopsHostCommandDTO;

    public DevopsHostCommandDTO getDevopsHostCommandDTO() {
        return devopsHostCommandDTO;
    }

    public void setDevopsHostCommandDTO(DevopsHostCommandDTO devopsHostCommandDTO) {
        this.devopsHostCommandDTO = devopsHostCommandDTO;
    }


    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

}
