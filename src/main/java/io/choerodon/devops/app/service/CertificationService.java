package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.C7nCertificationCreateOrUpdateVO;
import io.choerodon.devops.api.vo.CertificationRespVO;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDTO;
import io.choerodon.devops.infra.enums.CertificationStatus;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:43
 * Description:
 */
public interface CertificationService {

    /**
     * 创建证书资源
     *
     * @param projectId        项目id
     * @param certificationDTO 证书
     */
    void createOrUpdateCertification(Long projectId, C7nCertificationCreateOrUpdateVO certificationDTO,
                                     MultipartFile key, MultipartFile cert);

    C7nCertification getV1Alpha1C7nCertification(String name, String type, List<String> domains,
                                                 String keyContent, String certContent, String envCode);

    C7nCertification getV1C7nCertification(String name, String type, List<String> domains,
                                                 String keyContent, String certContent, String envCode);

    void deleteById(Long projectId, Long certId);

    void certDeleteByGitOps(Long certId);

    Page<CertificationVO> pageByOptions(Long projectId, Long envId, PageRequest pageable, String params);

    List<CertificationVO> queryActiveCertificationByDomain(Long projectId, Long envId, String domain);

    Boolean checkCertNameUniqueInEnv(Long envId, String certName, Long certId);


    /**
     * 根据证书名称查询证书
     *
     * @param envId    环境ID
     * @param certName 证书名称
     * @return CertificationVO
     */
    CertificationVO queryByName(Long envId, String certName);

    /**
     * 根据证书ID查询证书
     *
     * @param certId 证书ID
     * @return 证书信息
     */
    CertificationRespVO queryByCertId(Long certId);

    Long createCertCommand(String type, Long certId, Long userId);

    List<ProjectCertificationVO> listProjectCertInProject(Long projectId);

    CertificationDTO baseCreate(CertificationDTO certificationVO);

    void storeNotifyInfo(CertificationDTO certificationDTO);

    void updateNotifyInfo(CertificationDTO certificationDTO);

    CertificationDTO baseUpdate(CertificationDTO certificationDTO);

    CertificationDTO baseQueryById(Long certId);

    CertificationDTO baseQueryByEnvAndName(Long envId, String name);

    Page<CertificationDTO> basePage(Long projectId, Long envId, PageRequest pageable, String params);

    List<CertificationDTO> baseQueryActiveByDomain(Long projectId, Long clusterId, String domain);

    void baseUpdateCommandId(CertificationDTO certificationDTO);

    void baseUpdateValidField(CertificationDTO inputCertificationDTO);

    void baseUpdateCertFileId(CertificationDTO inputCertificationDTO);

    void baseClearValidField(Long certId);

    void baseDeleteById(Long certId);

    Long baseStoreCertFile(CertificationFileDTO certificationFileDTO);

    void baseUpdateCertFile(CertificationFileDTO certificationFileDTO);

    CertificationFileDTO baseQueryCertFile(Long certId);

    List<CertificationDTO> baseListByEnvId(Long envId);

    void baseUpdateSkipProjectPermission(CertificationDTO certificationDTO);

    CertificationDTO baseQueryByProjectAndName(Long projectId, String name);

    List<CertificationDTO> baseListByOrgCertId(Long orgCertId);

    List<CertificationDTO> baseListByProject(Long projectId, Long organizationId);

    void updateStatus(CertificationDTO certificationDTO);

    int updateStatusIfOperating(Long certId, CertificationStatus certificationStatus);
}
