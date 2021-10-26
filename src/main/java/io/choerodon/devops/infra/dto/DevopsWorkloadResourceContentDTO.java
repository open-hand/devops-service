package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "devops_workload_resource_content")
public class DevopsWorkloadResourceContentDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long workloadId;
    private String type;
    private String content;

    public DevopsWorkloadResourceContentDTO() {
    }

    public DevopsWorkloadResourceContentDTO(Long workloadId, String type, String content) {
        this.workloadId = workloadId;
        this.type = type;
        this.content = content;
    }

    public Long getWorkloadId() {
        return workloadId;
    }

    public void setWorkloadId(Long workloadId) {
        this.workloadId = workloadId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
