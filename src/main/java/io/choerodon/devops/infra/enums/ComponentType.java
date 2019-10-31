package io.choerodon.devops.infra.enums;

public enum ComponentType{
    /**
     * 普罗米修斯yaml配置转换
     */
    PROMETHEUS("prometheus");

    private String type;

    ComponentType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }
}
