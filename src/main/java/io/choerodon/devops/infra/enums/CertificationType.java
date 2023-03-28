package io.choerodon.devops.infra.enums;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:29
 * Description:
 */
public enum CertificationType {
    REQUEST("request"),
    UPLOAD("upload"),
    CHOOSE("choose");

    private String type;

    CertificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
