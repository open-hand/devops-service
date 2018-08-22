package io.choerodon.devops.domain.application.valueobject.certification;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:57
 * Description:
 */
public class CertificationMetadata {
    private String name;
    private String namespace;

    public CertificationMetadata() {
    }

    public CertificationMetadata(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
