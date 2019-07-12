package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:36 2019/7/12
 * Description:
 */
public interface DevopsEnvCommandService {
    DevopsEnvCommandVO baseCreate(DevopsEnvCommandVO devopsEnvCommandVO);

    DevopsEnvCommandVO baseQueryByObject(String objectType, Long objectId);

    DevopsEnvCommandVO baseUpdate(DevopsEnvCommandVO devopsEnvCommandVO);

    DevopsEnvCommandVO baseQuery(Long id);

    List<DevopsEnvCommandVO> baseListByEnvId(Long envId);

    List<DevopsEnvCommandVO> baseQueryInstanceCommand(String objectType, Long objectId);

    PageInfo<DevopsEnvCommandVO> baseListByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime);

    void baseDelete(Long commandId);

    List<DevopsEnvCommandVO> baseListByObjectAll(String objectType, Long objectId);

    void baseDeleteCommandById(DevopsEnvCommandVO devopsEnvCommandVO);
}
