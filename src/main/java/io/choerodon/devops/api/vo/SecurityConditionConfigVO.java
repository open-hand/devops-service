package io.choerodon.devops.api.vo;

/**
 * Created by wangxiang on 2021/4/20
 */
public class SecurityConditionConfigVO {
    private String level;
    private String symbol;
    private Integer condition;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getCondition() {
        return condition;
    }

    public void setCondition(Integer condition) {
        this.condition = condition;
    }
}
