package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈环境VO〉
 *
 * @author wanghao
 * @Date 2019/12/4 16:02
 */
public class DevopsEnvironmentVO {
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
