package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface AppServiceVersionValueService {

    AppServiceVersionValueDTO baseCreate(AppServiceVersionValueDTO
                                                 appServiceVersionValueDTO);

    AppServiceVersionValueDTO baseQuery(Long appVersionValueId);

}
