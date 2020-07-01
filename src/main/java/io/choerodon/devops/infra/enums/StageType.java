package io.choerodon.devops.infra.enums;

public enum StageType {
    CD("CD"),
    CI("CI");
    private String type;

    StageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
