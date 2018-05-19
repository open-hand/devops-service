package io.choerodon.devops.domain.application.valueobject;

/**
 * Created by Zenger on 2018/4/4.
 */
public class ChatV {

    private String name;
    private String repository;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
