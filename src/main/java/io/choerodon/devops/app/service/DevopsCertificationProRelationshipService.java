package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCertificationProRelationshipDTO;

/**
 * @author zmf
 */
public interface DevopsCertificationProRelationshipService {
    void baseInsertRelationship(DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO);

    List<DevopsCertificationProRelationshipDTO> baseListByCertificationId(Long certificationId);

    void baseDelete(DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO);

    void baseDeleteByCertificationId(Long certificationId);
}
