package io.choerodon.devops.api.dto;

/**
 * Created by younger on 2018/4/25.
 */
public class ReplicaSetDTO {
    private String name;
    private Long desired;
    private Long current;
    private Long ready;
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

    public Long getReady() {
        return ready;
    }

    public void setReady(Long ready) {
        this.ready = ready;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
