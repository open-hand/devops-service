package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface AppServiceVersionValueService {

    AppServiceVersionValueDTO baseCreate(AppServiceVersionValueDTO
                                                 appServiceVersionValueDTO);

    AppServiceVersionValueDTO baseUpdate(AppServiceVersionValueDTO appServiceVersionValueDTO);

    AppServiceVersionValueDTO baseQuery(Long appServiceServiceValueId);

    void baseDeleteById(Long appServiceServiceValueId);

    void deleteByIds(Set<Long> valueIds);
}
