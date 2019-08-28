package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:18 2019/6/28
 * Description:
 */
@Table(name = "devops_app_service_share_resource")
public class AppServiceShareResourceDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shareId;
    private Long projectId;

    public AppServiceShareResourceDTO() {
    }

    public AppServiceShareResourceDTO(Long shareId, Long projectId) {
        this.shareId = shareId;
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
