package io.choerodon.devops.api.vo;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by younger on 2018/4/25.
 */
public class IngressVO {
    private String name;
    private String hosts;
    private String address;
    private String ports;
    private String age;
    @ApiModelProperty("这个ingress所关联的service的名称集合")
    private Set<String> services;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Set<String> getServices() {
        return services;
    }

    public void setServices(Set<String> services) {
        this.services = services;
    }
}
