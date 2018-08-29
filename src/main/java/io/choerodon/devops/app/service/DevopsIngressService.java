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
     *
     * @param devopsIngressDTO 域名参数
     * @param projectId        项目Id
     */
    void addIngress(DevopsIngressDTO devopsIngressDTO, Long projectId, boolean gitops);

    /**
     * 项目下更新域名
     *
     * @param id               域名Id
     * @param devopsIngressDTO 域名参数
     * @param projectId        项目Id
     */
    void updateIngress(Long id, DevopsIngressDTO devopsIngressDTO, Long projectId, boolean gitops);

    /**
     * 项目下查询域
     *
     * @param projectId   项目Id
     * @param pageRequest 分页参数
     * @param params      模糊查询参数
     * @return Page
     */
    Page<DevopsIngressDTO> getIngress(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下查询域名
     *
     * @param projectId 项目Id
     * @param ingressId 域名Id
     * @return DevopsIngressDTO
     */
    DevopsIngressDTO getIngress(Long projectId, Long ingressId);

    /**
     * 项目下删除域名
     *
     * @param ingressId 域名Id
     */
    void deleteIngress(Long ingressId, boolean gitops);

    /**
     * 检查域名唯一性
     *
     * @param envId 环境Id
     * @param name  域名name
     * @return boolean
     */
    Boolean checkName(Long envId, String name);

    /**
     * 检查域名唯一性
     *
     * @param domain 域名
     * @param path   路径
     * @param id     ingress ID
     * @return boolean
     */
    Boolean checkDomainAndPath(Long id, String domain, String path);

    /**
     * 项目下创建域名
     *
     * @param host     主机
     * @param name     域名名称
     * @param certName 证书名称
     * @return V1beta1Ingress
     */
    V1beta1Ingress createIngress(String host, String name, String certName);

    /**
     * 项目下创建path
     *
     * @param hostPath  主机path
     * @param serviceId 网络Id
     * @return V1beta1HTTPIngressPath
     */
    V1beta1HTTPIngressPath createPath(String hostPath, Long serviceId, Long port);


    /**
     * 环境总览域名查询
     *
     * @param projectId   项目Id
     * @param pageRequest 分页参数
     * @param params      模糊查询参数
     * @return Page
     */
    Page<DevopsIngressDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String params);
}
