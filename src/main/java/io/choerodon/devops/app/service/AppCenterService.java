package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.AppCenterDetailVO;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public interface AppCenterService {

    AppCenterDetailVO appCenterDetail(Long projectId, Long appCenterId);
}
