package io.choerodon.devops.api.vo;

public class MiddlewareInventoryVO {
    private StringBuilder all = new StringBuilder();
    private StringBuilder chrony = new StringBuilder();
    private StringBuilder redis = new StringBuilder();

    public StringBuilder getAll() {
        return all;
    }

    public void setAll(StringBuilder all) {
        this.all = all;
    }

    public StringBuilder getChrony() {
        return chrony;
    }

    public void setChrony(StringBuilder chrony) {
        this.chrony = chrony;
    }

    public StringBuilder getRedis() {
        return redis;
    }

    public void setRedis(StringBuilder redis) {
        this.redis = redis;
    }
}
