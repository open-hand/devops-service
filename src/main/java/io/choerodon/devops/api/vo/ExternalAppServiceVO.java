package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.AppExternalConfigDTO;

/**
 * Created by younger on 2018/3/28.
 */
public class ExternalAppServiceVO {

    @Encrypt
    private Long id;

    @ApiModelProperty("服务名称/必填")
    @NotNull(message = "{devops.app.name.null}")
    @Length(message = "{devops.app.service.name.length}", min = 1, max = 40)
    private String name;

    @ApiModelProperty("服务code/必填")
    @NotNull(message = "{devops.app.code.null}")
    private String code;

    @ApiModelProperty("项目id/必填")
    private Long projectId;

    @ApiModelProperty("服务类型/必填")
    @NotNull(message = "{devops.app.type.null}")
    private String type;

    private AppExternalConfigDTO appExternalConfigDTO;


    public AppExternalConfigDTO getAppExternalConfigDTO() {
        return appExternalConfigDTO;
    }

    public void setAppExternalConfigDTO(AppExternalConfigDTO appExternalConfigDTO) {
        this.appExternalConfigDTO = appExternalConfigDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
