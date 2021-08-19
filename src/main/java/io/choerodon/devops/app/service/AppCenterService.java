package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.AppCenterEnvDetailVO;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public interface AppCenterService {

    AppCenterEnvDetailVO appCenterDetail(Long projectId, Long appCenterId);
}
