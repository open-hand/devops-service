package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandLogVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:48 2019/7/12
 * Description:
 */
public interface DevopsEnvCommandLogService {
    DevopsEnvCommandLogVO baseCreate(DevopsEnvCommandLogVO devopsEnvCommandLogE);

    DevopsEnvCommandLogVO baseQuery(Long logId);

    List<DevopsEnvCommandLogVO> baseListByDeployId(Long deployId);

    void baseDeleteByInstanceId(Long instanceId);

    void baseDeleteByCommandId(Long commandId);
}
