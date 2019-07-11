package io.choerodon.devops.infra.enums;

/**
 * Created by Sheep on 2019/5/6.
 */
public enum Rate {

    MAJOR("MAJOR"),
    INFO("INFO"),
    MINOR("MINOR"),
    CRITICAL("CRITICAL"),
    BLOCKER("BLOCKER");

    private String rate;

    Rate(String rate) {
        this.rate = rate;
    }

    public String getRate() {
        return rate;
    }
}
