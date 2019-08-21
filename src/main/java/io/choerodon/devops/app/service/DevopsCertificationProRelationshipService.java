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

    /**
     * 批量插入，忽视已经存在的关联关系
     *
     * @param certId     证书id
     * @param projectIds 项目id
     */
    void batchInsertIgnore(Long certId, List<Long> projectIds);
}
