package io.choerodon.devops.infra.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:36 2019/9/27
 * Description:
 */
public enum OrgPublishMarketStatus {
    DEPLOY_ONLY("mkt_deploy_only"),
    DOWNLOAD_ONLY("mkt_code_only"),
    ALL("mkt_code_deploy");

    private static HashMap<String, OrgPublishMarketStatus> valuesMap = new HashMap<>(6);

    static {
        OrgPublishMarketStatus[] var0 = values();

        for (OrgPublishMarketStatus objectType : var0) {
            valuesMap.put(objectType.type, objectType);
        }
    }

    private String type;

    OrgPublishMarketStatus(String type) {
        this.type = type;
    }

    @JsonCreator
    public static OrgPublishMarketStatus forValue(String value) {
        return valuesMap.get(value);
    }

    public String getType() {
        return type;
    }
}
