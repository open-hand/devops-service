package io.choerodon.devops.api.vo;

/**
 * Creator: Runge
 * Date: 2018/5/31
 * Time: 15:48
 * Description:
 */
public class AppServiceVersionUploadVO {
    private Long id;
    private String version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
