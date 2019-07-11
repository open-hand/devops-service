package io.choerodon.devops.api.vo;

/**
 * Creator: Runge
 * Date: 2018/5/17
 * Time: 10:09
 * Description:
 */
public class DevopsEnvPodContainerDTO {
    private Long id;
    private Long podId;
    private String containerName;

    public DevopsEnvPodContainerDTO() {
    }

    /**
     * 覆写构造方法
     */
    public DevopsEnvPodContainerDTO(Long id, Long podId, String containerName) {

        this.id = id;
        this.podId = podId;
        this.containerName = containerName;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPodId() {
        return podId;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
}
