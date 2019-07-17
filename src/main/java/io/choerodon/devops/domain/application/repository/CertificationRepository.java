package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.devops.infra.dto.CertificationFileDO;
=======
import io.choerodon.devops.infra.dataobject.CertificationFileDO;
>>>>>>> [IMP] 修改AppControler重构
=======
import io.choerodon.devops.infra.dto.CertificationFileDTO;
>>>>>>> [REF] refactor CertificationController. to be continued.

/**
 * Created by n!Ck
 * Date: 2018/8/21
 * Time: 15:04
 * Description:
 */
public interface CertificationRepository {

    CertificationE baseCreate(CertificationE certificationE);

    CertificationE baseQueryById(Long certId);

    CertificationE baseQueryByEnvAndName(Long envId, String name);

    PageInfo<CertificationVO> basePage(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params);

<<<<<<< HEAD
    List<CertificationDTO> getActiveByDomain(Long projectId,Long clusterId, String domain);
=======
    List<CertificationVO> baseGetActiveByDomain(Long projectId, Long clusterId, String domain);
>>>>>>> [REF] refactor CertificationRepository

    void baseUpdateStatus(CertificationE certificationE);

    void baseUpdateCommandId(CertificationE certificationE);

    void baseUpdateValidField(CertificationE certificationE);

    void baseUpdateCertFileId(CertificationE certificationE);

    void baseClearValidField(Long certId);

    void baseDeleteById(Long certId);

    Boolean baseCheckCertNameUniqueInEnv(Long envId, String certName);

    Long baseStoreCertFile(CertificationFileDTO certificationFileDTO);

    CertificationFileDTO baseGetCertFile(Long certId);

    List<CertificationE> baseListByEnvId(Long envId);

    void baseUpdateSkipProjectPermission(CertificationE certificationE);

    CertificationE baseQueryByOrgAndName(Long orgId, String name);

    List<CertificationE> baseListByOrgCertId(Long orgCertId);

    List<CertificationVO> baseListByProject(Long projectId, Long organizationId);
}
