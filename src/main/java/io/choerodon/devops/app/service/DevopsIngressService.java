package io.choerodon.devops.app.service;

import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1Ingress;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsIngressDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 16:01
 * Description:
 */
public interface DevopsIngressService {

    /**
     * 项目下创建域名
     */
    void addIngress(DevopsIngressDTO devopsIngressDTO, Long projectId);

    /**
     * 项目下更新域名
     */
    void updateIngress(Long id, DevopsIngressDTO devopsIngressDTO, Long projectId);

    /**
     * 项目下查询域
     */
    Page<DevopsIngressDTO> getIngress(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下查询域名
     */
    DevopsIngressDTO getIngress(Long projectId, Long ingressId);

    /**
     * 项目下删除域名
     */
    void deleteIngress(Long ingressId);

    /**
     * 检查域名唯一性
     */
    Boolean checkName(Long envId, String name);

    /**
     * 检查域名唯一性
     */
    Boolean checkDomainAndPath(String domain, String path);

    /**
     * 项目下创建域名
     */
    V1beta1Ingress createIngress(String host, String name, String namspace);

    /**
     * 项目下创建path
     */
    V1beta1HTTPIngressPath createPath(String hostPath, Long serviceId);
}
