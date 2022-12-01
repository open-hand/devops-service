package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCdApiTestInfoDTO;

/**
 * devops_cd_api_test_info(DevopsCdApiTestInfo)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-20 09:57:26
 */
public interface DevopsCdApiTestInfoService {

    void baseCreate(DevopsCdApiTestInfoDTO devopsCdApiTestInfoDTO);

    DevopsCdApiTestInfoDTO queryById(Long deployInfoId);
}

