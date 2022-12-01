package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCdHostDeployInfoDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/14 10:02
 */
public interface DevopsCdHostDeployInfoService {

    DevopsCdHostDeployInfoDTO baseCreate(DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO);


    DevopsCdHostDeployInfoDTO queryById(Long id);

    void baseUpdate(DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO);
}
