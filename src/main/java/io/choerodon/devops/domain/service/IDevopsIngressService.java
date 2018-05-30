package io.choerodon.devops.domain.service;

/**
 * Created by Zenger on 2018/5/14.
 */
public interface IDevopsIngressService {

    void createIngress(String ingressYaml, String name, String namespace, Long commandId);

    void deleteIngress(String name, String namespace, Long commandId);
}
