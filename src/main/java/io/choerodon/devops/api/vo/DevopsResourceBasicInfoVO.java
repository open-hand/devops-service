package io.choerodon.devops.api.vo;

/**
 * 资源的基本信息，id和名称
 *
 * @author zmf
 */
public class DevopsResourceBasicInfoVO {
    private Long id;
    private String name;

    public DevopsResourceBasicInfoVO() {
    }

    public DevopsResourceBasicInfoVO(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
