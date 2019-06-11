package io.choerodon.devops.infra.dataobject;

import javax.persistence.*;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:32 2019/4/3
 * Description:
 */
@Table(name = "devops_pipeline_task")
public class PipelineTaskDO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long stageId;
    private String name;
    private String type;
    private Long appDeployId;
    private Integer isCountersigned;
    private Long projectId;

    @Transient
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAppDeployId() {
        return appDeployId;
    }

    public void setAppDeployId(Long appDeployId) {
        this.appDeployId = appDeployId;
    }

    public Integer getIsCountersigned() {
        return isCountersigned;
    }

    public void setIsCountersigned(Integer isCountersigned) {
        this.isCountersigned = isCountersigned;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
