package io.choerodon.devops.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 证书及其相关的Ingress名称
 * @author zmf
 */
public class CertificationRespVO extends DevopsResourceDataInfoVO {
    private Long id;
    private String name;
    private String commonName;
    private List<String> DNSNames;
    private List<String> ingresses;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * 不使用此注解，则DNSNames这个字段会被序列化为dnsnames
     */
    @JsonProperty(value = "DNSNames")
    public List<String> getDNSNames() {
        return DNSNames;
    }

    public void setDNSNames(List<String> DNSNames) {
        this.DNSNames = DNSNames;
    }

    public List<String> getIngresses() {
        return ingresses;
    }

    public void setIngresses(List<String> ingresses) {
        this.ingresses = ingresses;
    }
}
