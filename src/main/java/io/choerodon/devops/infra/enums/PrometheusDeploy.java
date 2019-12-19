package io.choerodon.devops.infra.enums;

/**
 * @author: 25499
 * @date: 2019/11/11 10:38
 * @description:
 */
public enum  PrometheusDeploy {
    SUCCESSED("successed"),
    OPERATING("operating"),
    WAITING("waiting"),
    FAILED("failed");

    private String staus;

    PrometheusDeploy(String staus) {
        this.staus = staus;
    }

    public String getStaus() {
        return staus;
    }
}
