package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandValueVO;

public interface DevopsEnvCommandValueRepository {

    DevopsEnvCommandValueVO baseCreate(DevopsEnvCommandValueVO devopsEnvCommandValueE);

    void baseDeleteById(Long commandId);

    void baseUpdateById(Long valueId, String value);

}
