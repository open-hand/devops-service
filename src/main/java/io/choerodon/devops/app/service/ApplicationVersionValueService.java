package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.ApplicationVersionValueDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface ApplicationVersionValueService {

    ApplicationVersionValueDTO baseCreate(ApplicationVersionValueDTO
                                                  applicationVersionValueDTO);

    ApplicationVersionValueDTO baseQuery(Long appVersionValueId);

}
