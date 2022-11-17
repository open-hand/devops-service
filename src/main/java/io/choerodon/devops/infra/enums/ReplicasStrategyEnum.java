package io.choerodon.devops.infra.enums;

public enum ReplicasStrategyEnum {
    VALUES("values"),
    REPLICAS("replicas");

    private  String strategy;

    ReplicasStrategyEnum(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy(){
        return strategy;
    }
}
