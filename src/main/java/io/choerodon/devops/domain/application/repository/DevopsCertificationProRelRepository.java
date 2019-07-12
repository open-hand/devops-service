package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsCertificationProRelE;

public interface DevopsCertificationProRelRepository {

    void baseInsertRelationship(DevopsCertificationProRelE devopsCertificationProRelE);

    List<DevopsCertificationProRelE> baseListByCertificationId(Long certificationId);

    void baseDelete(DevopsCertificationProRelE devopsCertificationProRelE);

    void baseDeleteByCertificationId(Long certificationId);
}
