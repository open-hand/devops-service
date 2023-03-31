package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CertificationNotifyObject;
import io.choerodon.devops.infra.dto.CertificationNoticeDTO;

public interface DevopsCertificationNoticeService {
    void batchCreate(Long id, List<CertificationNotifyObject> notifyObjects);

    void batchUpdate(Long certificationId, List<CertificationNotifyObject> notifyObjects);

    List<CertificationNoticeDTO> listByCertificationIds(List<Long> certificationIds);

    List<CertificationNoticeDTO> listByCertificationId(Long certificationId);
}
