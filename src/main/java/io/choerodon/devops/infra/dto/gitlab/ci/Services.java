package io.choerodon.devops.infra.dto.gitlab.ci;

/**
 * Created by wangxiang on 2021/4/19
 */
public class Services {
    private String name;
    private String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
