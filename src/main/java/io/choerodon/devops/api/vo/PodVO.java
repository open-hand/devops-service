package io.choerodon.devops.api.vo;

/**
 * Created by younger on 2018/4/25.
 */
public class PodVO {
    private String name;
    private Long ready;
    private Long desire;
    private String status;
    private Long restarts;
    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getReady() {
        return ready;
    }

    public void setReady(Long ready) {
        this.ready = ready;
    }

    public Long getDesire() {
        return desire;
    }

    public void setDesire(Long desire) {
        this.desire = desire;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRestarts() {
        return restarts;
    }

    public void setRestarts(Long restarts) {
        this.restarts = restarts;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
