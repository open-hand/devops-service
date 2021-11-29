package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsCiStepVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:18
 */
public interface DevopsCiStepHandler {

    void save(DevopsCiStepVO devopsCiStepVO);

    String getType();
}
