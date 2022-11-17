package io.choerodon.devops.infra.enums;

public enum ReplicasStrategyEnum {
    VALUES("values"),
    DEPLOYMENT("deployment");

    private  String strategy;

    ReplicasStrategyEnum(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy(){
        return strategy;
    }
}
