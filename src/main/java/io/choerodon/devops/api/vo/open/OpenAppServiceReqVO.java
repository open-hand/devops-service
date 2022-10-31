package io.choerodon.devops.api.vo.open;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

/**
 * Created by younger on 2018/3/28.
 */
public class OpenAppServiceReqVO {

    private Long id;

    @ApiModelProperty("服务名称/必填")
    @NotNull(message = "devops.app.name.null")
    @Length(message = "devops.app.service.name.length", min = 1, max = 40)
    private String name;

    @ApiModelProperty("服务code/必填")
    @NotNull(message = "{devops.app.code.null}")
    private String code;

    @ApiModelProperty("项目id/必填")
    private Long projectId;

    @ApiModelProperty("服务类型/必填 normal")
    @NotNull(message = "{devops.app.type.null}")
    private String type;

    @ApiModelProperty("项目所有者邮箱")
    @NotNull(message = "devops.email.null")
    private String email;

    private String imgUrl;


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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
