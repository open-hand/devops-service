package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.domain.application.entity.CertificationE;
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

    Page<CertificationDTO> page(Long projectId, PageRequest pageRequest, String params);

    List<CertificationDTO> getActiveByDomain(Long envId, String domain);

    void updateStatus(CertificationE certificationE);

    void updateCommandId(CertificationE certificationE);

    void deleteById(Long certId);

    Boolean checkCertNameUniqueInEnv(Long envId, String certName);
}
