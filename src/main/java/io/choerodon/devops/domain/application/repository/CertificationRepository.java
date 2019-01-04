package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.infra.dataobject.CertificationFileDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/8/21
 * Time: 15:04
 * Description:
 */
public interface CertificationRepository {

    CertificationE create(CertificationE certificationE);

    CertificationE queryById(Long certId);

    CertificationE queryByEnvAndName(Long envId, String name);

    Page<CertificationDTO> page(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params);

    List<CertificationDTO> getActiveByDomain(Long projectId, String domain);

    void updateStatus(CertificationE certificationE);

    void updateCommandId(CertificationE certificationE);

    void updateValid(CertificationE certificationE);

    void updateCertFileId(CertificationE certificationE);

    void clearValid(Long certId);

    void deleteById(Long certId);

    Boolean checkCertNameUniqueInEnv(Long envId, String certName);

    Long storeCertFile(CertificationFileDO certificationFileDO);

    CertificationFileDO getCertFile(Long certId);

    List<CertificationE> listByEnvId(Long envId);

    void updateSkipProjectPermission(CertificationE certificationE);

    CertificationE queryByOrgAndName(Long orgId, String name);

    List<CertificationE> listByOrgCertId(Long orgCertId);

    List<CertificationDTO> listByProject(Long projectId, Long organizationId);
}
