package io.choerodon.devops.api.vo.template;


import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */


public class CiTemplateStepCategoryVO {
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long sourceId;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Boolean builtIn;

    public Boolean getBuiltIn() {
        return builtIn;
    }


    @ApiModelProperty(value = "创建时间")
    private Date creationDate;

    @ApiModelProperty(value = "创建者")
    private Long createdBy;


    @ApiModelProperty(value = "关联模板的总数")
    private Long templateNumber;


    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

}

