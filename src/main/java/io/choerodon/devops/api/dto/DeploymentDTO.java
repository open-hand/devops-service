package io.choerodon.devops.api.dto;

/**
 * Created by younger on 2018/4/25.
 */
public class DeploymentDTO {
    private String name;
    private Long desired;
    private Long current;
    private Long upToDate;
    private Long available;
    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDesired() {
        return desired;
    }

    public void setDesired(Long desired) {
        this.desired = desired;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getUpToDate() {
        return upToDate;
    }

    public void setUpToDate(Long upToDate) {
        this.upToDate = upToDate;
    }

    public Long getAvailable() {
        return available;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
