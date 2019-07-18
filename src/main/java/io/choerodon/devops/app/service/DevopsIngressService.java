package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1Ingress;

public interface DevopsIngressService {

    /**
     * 项目下创建域名
     *
     * @param devopsIngressVO 域名参数
     * @param projectId       项目Id
     */
    void createIngress(DevopsIngressVO devopsIngressVO, Long projectId);

    /**
     * 项目下创建域名,GitOps
     *
     * @param devopsIngressVO 域名参数
     * @param projectId       项目Id
     */
    void createIngressByGitOps(DevopsIngressVO devopsIngressVO, Long projectId, Long userId);

    /**
     * 项目下更新域名
     *
     * @param id              域名Id
     * @param devopsIngressVO 域名参数
     * @param projectId       项目Id
     */
    void updateIngress(Long id, DevopsIngressVO devopsIngressVO, Long projectId);


    /**
     * 项目下更新域名,GitOps
     *
     * @param id              域名Id
     * @param devopsIngressVO 域名参数
     * @param projectId       项目Id
     */
    void updateIngressByGitOps(Long id, DevopsIngressVO devopsIngressVO, Long projectId, Long userId);


    /**
     * 项目下查询域名
     *
     * @param projectId 项目Id
     * @param ingressId 域名Id
     * @return DevopsIngressVO
     */
    DevopsIngressVO queryIngress(Long projectId, Long ingressId);

    /**
     * 项目下删除域名
     *
     * @param ingressId 域名Id
     */
    void deleteIngress(Long ingressId);


    /**
     * 项目下删除域名,GitOps
     *
     * @param ingressId 域名Id
     */
    void deleteIngressByGitOps(Long ingressId);

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
    Boolean checkDomainAndPath(Long envId, String domain, String path, Long id);

//    /**
//     * 项目下创建域名
//     *
//     * @param host     主机
//     * @param name     域名名称
//     * @param certName 证书名称
//     * @return V1beta1Ingress
//     */
//    V1beta1Ingress initV1beta1Ingress(String host, String name, String certName);
//
//
//    /**
//     * 项目下创建path
//     *
//     * @param hostPath    主机path
//     * @param serviceName 网络name
//     * @return V1beta1HTTPIngressPath
//     */
//    V1beta1HTTPIngressPath createPath(String hostPath, String serviceName, Long port);


    /**
     * 环境总览域名查询
     *
     * @param projectId   项目Id
     * @param pageRequest 分页参数
     * @param params      模糊查询参数
     * @return Page
     */
    PageInfo<DevopsIngressVO> pageByEnv(Long projectId, Long envId, PageRequest pageRequest, String params);
}
