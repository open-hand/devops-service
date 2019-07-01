package io.choerodon.devops.domain.application.entity;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:08 2019/6/28
 * Description:
 */
public class AppShareResourceE {
    private Long id;
    private Long shareId;
    private Long projectId;

    public AppShareResourceE(Long shareId, Long projectId) {
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
