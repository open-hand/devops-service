package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsCertificationProRelE;

public interface DevopsCertificationProRelRepository {

    void insert(DevopsCertificationProRelE devopsCertificationProRelE);

    List<DevopsCertificationProRelE> listByCertId(Long certId);

    void delete(DevopsCertificationProRelE devopsCertificationProRelE);

    void deleteByCertId(Long certId);
}
