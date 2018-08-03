package io.choerodon.devops.domain.service;

import io.kubernetes.client.models.V1beta1Ingress;

import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by Zenger on 2018/5/14.
 */
public interface IDevopsIngressService {

    void createIngress(V1beta1Ingress v1beta1Ingress, DevopsEnvironmentE devopsEnvironmentE, Long userId, String type);

    void deleteIngress(Long ingressId, DevopsEnvironmentE devopsEnvironmentE, Long userId);
}
