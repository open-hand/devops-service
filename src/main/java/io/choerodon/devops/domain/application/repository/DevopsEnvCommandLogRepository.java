package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandLogE;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvCommandLogRepository {

    DevopsEnvCommandLogE create(DevopsEnvCommandLogE devopsEnvCommandLogE);

    DevopsEnvCommandLogE query(Long logId);

    List<DevopsEnvCommandLogE> queryByDeployId(Long deployId);

    void deletePreInstanceCommandLog(Long instanceId);

    void deleteByCommandId(Long commandId);
}
