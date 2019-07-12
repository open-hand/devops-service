package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.C7nCertificationDTO;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.OrgCertificationDTO;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:43
 * Description:
 */
public interface CertificationService {

    /**
     * 创建c7n证书
     *
     * @param projectId        项目id
     * @param certificationDTO 证书
     */
    void baseCreate(Long projectId, C7nCertificationDTO certificationDTO,
                    MultipartFile key, MultipartFile cert, Boolean isGitOps);

    C7nCertification getC7nCertification(String name, String type, List<String> domains,
                                         String keyContent, String certContent, String envCode);

    void deleteById(Long certId);

    void certDeleteByGitOps(Long certId);

    PageInfo<CertificationVO> basePage(Long projectId, Long envId, PageRequest pageRequest, String params);

    List<CertificationVO> getActiveByDomain(Long projectId, Long envId, String domain);

    Boolean checkCertNameUniqueInEnv(Long envId, String certName);


    /**
     * 根据证书名称查询证书
     *
     * @param envId    环境ID
     * @param certName 证书名称
     * @return CertificationVO
     */
    CertificationVO queryByName(Long envId, String certName);

    Long createCertCommandE(String type, Long certId, Long userId);

    List<OrgCertificationDTO> baseListByProject(Long projectId);

    CertificationDTO baseCreate(CertificationDTO certificationVO);

    CertificationDTO baseQueryById(Long certId);

    CertificationVO baseQueryByEnvAndName(Long envId, String name);

    PageInfo<CertificationVO> basePage(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params);

    List<CertificationVO> baseQueryActiveByDomain(Long projectId, Long clusterId, String domain);

    void baseUpdateStatus(CertificationDTO certificationDTO);

    void baseUpdateCommandId(CertificationDTO certificationDTO);

    void baseUpdateValidField(CertificationDTO inputCertificationDTO);

    void baseUpdateCertFileId(CertificationDTO inputCertificationDTO);

    void baseClearValidField(Long certId);

    void baseDeleteById(Long certId);

    Boolean baseCheckCertNameUniqueInEnv(Long envId, String certName);

    Long baseStoreCertFile(CertificationFileDO certificationFileDO);

    CertificationFileDO baseQueryCertFile(Long certId);

    List<CertificationDTO> baseListByEnvId(Long envId);

    void baseUpdateSkipProjectPermission(CertificationDTO certificationDTO);

    CertificationVO baseQueryByOrgAndName(Long orgId, String name);

    List<CertificationDTO> baseListByOrgCertId(Long orgCertId);

    List<CertificationVO> baseListByProject(Long projectId, Long organizationId);
}
