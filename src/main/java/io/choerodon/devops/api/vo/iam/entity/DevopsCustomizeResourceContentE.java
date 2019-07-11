package io.choerodon.devops.api.vo.iam.entity;

/**
 * Created by Sheep on 2019/6/26.
 */

public class DevopsCustomizeResourceContentE {

    private Long id;
    private String content;


    public DevopsCustomizeResourceContentE() {

    }

    public DevopsCustomizeResourceContentE(Long id) {
        this.id = id;
    }

    public DevopsCustomizeResourceContentE(String content) {
        this.content = content;
    }

    public DevopsCustomizeResourceContentE(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
