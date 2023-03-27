package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.C7nCertificationCreateOrUpdateVO;
import io.choerodon.devops.infra.dto.CertificationNoticeDTO;

public interface DevopsCertificationNoticeService {
    void batchCreate(Long id, List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects);

    void batchUpdate(Long certificationId, List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects);

    List<CertificationNoticeDTO> listByCertificationIds(List<Long> certificationIds);
}
