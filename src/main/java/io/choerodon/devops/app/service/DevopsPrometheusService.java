package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;

/**
 * @author: 25499
 * @date: 2019/10/28 15:52
 * @description:
 */
public interface DevopsPrometheusService {
    void create(DevopsPrometheusDTO devopsPrometheusDTO);

}
