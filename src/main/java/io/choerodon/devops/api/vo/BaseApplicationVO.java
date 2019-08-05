package io.choerodon.devops.api.vo;

/**
 * 为了返回应用最基本的信息
 *
 * @author zmf
 */
public class BaseApplicationVO {
    private Long id;
    private String name;

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
