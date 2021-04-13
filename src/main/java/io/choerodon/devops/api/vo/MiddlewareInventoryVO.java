package io.choerodon.devops.api.vo;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum;

public class MiddlewareInventoryVO {
    private StringBuilder all;
    private StringBuilder chrony;
    private StringBuilder redis;
    private StringBuilder mysql;

    public MiddlewareInventoryVO(DevopsMiddlewareTypeEnum middlewareTypeEnum) {
        this.all = new StringBuilder();
        this.all.append("[all]\n");
        this.chrony = new StringBuilder();
        this.chrony.append("[chrony]\n");
        switch (middlewareTypeEnum) {
            case REDIS:
                this.redis = new StringBuilder();
                this.redis.append("[redis]\n");
            case MYSQL:
                this.mysql = new StringBuilder();
                this.mysql.append("[mysql]\n");
            default:
                throw new CommonException("error.middleware.unsupported.type", middlewareTypeEnum.getType());
        }
    }

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

    public StringBuilder getMysql() {
        return mysql;
    }

    public void setMysql(StringBuilder mysql) {
        this.mysql = mysql;
    }

    public String getInventoryConfiguration() {
        String result = this.all.toString();
        result += this.chrony.toString();
        if (this.redis != null) {
            result += this.redis.toString();
        }
        if (this.mysql != null) {
            result += this.mysql.toString();
        }
        return result;
    }
}
