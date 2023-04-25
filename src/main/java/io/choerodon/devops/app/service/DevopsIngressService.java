package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.NginxIngressAnnotationVO;
import io.choerodon.devops.app.eventhandler.payload.IngressSagaPayload;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsIngressDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


public interface DevopsIngressService {

    /**
     * 项目下创建域名
     *
     * @param projectId       域名参数
     * @param devopsIngressVO 项目Id
     */
    void createIngress(Long projectId, DevopsIngressVO devopsIngressVO);

    /**
     * 为批量部署创建域名
     * 要求在调用方法前对环境和权限以及参数进行必要的校验
     *
     * @param devopsEnvironmentDTO 环境信息
     * @param userAttrDTO          用户信息
     * @param projectId            项目id
     * @param devopsIngressVO      域名信息
     * @return 域名信息处理后的结果
     */
    IngressSagaPayload createForBatchDeployment(
            DevopsEnvironmentDTO devopsEnvironmentDTO,
            UserAttrDTO userAttrDTO,
            Long projectId,
            DevopsIngressVO devopsIngressVO);

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
     * 项目下查询域名详情
     *
     * @param projectId 项目Id
     * @param ingressId 域名Id
     * @return DevopsIngressVO
     */
    DevopsIngressVO queryIngressDetailById(Long projectId, Long ingressId);

    /**
     * 项目下删除域名
     *
     * @param ingressId 域名Id
     */
    void deleteIngress(Long projectId, Long ingressId);


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


    /**
     * 环境总览域名查询
     *
     * @param projectId 项目Id
     * @param pageable  分页参数
     * @param params    模糊查询参数
     * @return Page
     */
    Page<DevopsIngressVO> pageByEnv(Long projectId, Long envId, PageRequest pageable, String params);

    void operateIngressBySaga(IngressSagaPayload ingressSagaPayload);

    DevopsIngressDTO baseQuery(Long ingressId);

    Page<DevopsIngressVO> basePageByOptions(Long projectId, Long envId, Long serviceId, PageRequest pageable, String params);

    List<DevopsIngressDTO> baseListByEnvId(Long envId);

    void deleteIngressAndIngressPathByEnvId(Long envId);

    void baseDelete(Long ingressId);

    Long baseUpdateStatus(Long envId, String name, String status);

    Boolean baseCheckName(Long envId, String name);

    Boolean baseCheckPath(Long envId, String domain, String path, Long id);

    DevopsIngressDTO baseCheckByEnvAndName(Long envId, String name);

    DevopsIngressDTO baseCreateIngress(DevopsIngressDTO devopsIngressDTO);

    void baseDeletePathByIngressId(Long ingressId);

    Boolean baseCheckByEnv(Long envId);

    List<DevopsIngressDTO> baseList();

    void updateStatus(Long envId, String name, String status);

    boolean operateForOldTypeIngressJudgeByClusterVersion(Long clusterId);

    boolean operateForOldTypeIngressJudgeByIngressVersion(String version);

    List<NginxIngressAnnotationVO> listNginxIngressAnnotation();

}
