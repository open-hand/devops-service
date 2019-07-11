package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CertificationDTO;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
<<<<<<< HEAD
import io.choerodon.devops.infra.dto.CertificationFileDO;
=======
import io.choerodon.devops.infra.dataobject.CertificationFileDO;
>>>>>>> [IMP] 修改AppControler重构

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

    PageInfo<CertificationDTO> page(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params);

    List<CertificationDTO> getActiveByDomain(Long projectId,Long clusterId, String domain);

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
