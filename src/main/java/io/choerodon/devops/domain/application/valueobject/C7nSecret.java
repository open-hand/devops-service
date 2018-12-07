package io.choerodon.devops.domain.application.valueobject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 下午4:33
 * Description:
 */
public class C7nSecret {
    private String apiVersion;
    private String kind;
    private Metadata metadata;
    private String type;
    private Map<String, String> stringData;

    public C7nSecret() {
        this.apiVersion = "v1";
        this.kind = "Secret";
        this.metadata = new Metadata();
        this.type = "Opaque";
        this.stringData = new HashMap<>();
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getStringData() {
        return stringData;
    }

    public void setStringData(Map<String, String> stringData) {
        this.stringData = stringData;
    }
}
