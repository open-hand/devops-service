package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:36 2019/7/12
 * Description:
 */
public interface DevopsEnvCommandService {
    DevopsEnvCommandDTO baseCreate(DevopsEnvCommandDTO devopsEnvCommandDTO);

    DevopsEnvCommandDTO baseQueryByObject(String objectType, Long objectId);

    DevopsEnvCommandDTO baseUpdate(DevopsEnvCommandDTO devopsEnvCommandDTO);

    DevopsEnvCommandDTO baseQuery(Long id);

    List<DevopsEnvCommandDTO> baseListByEnvId(Long envId);

    List<DevopsEnvCommandDTO> baseListInstanceCommand(String objectType, Long objectId);

    PageInfo<DevopsEnvCommandDTO> basePageByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime);

    void baseDelete(Long commandId);

    List<DevopsEnvCommandDTO> baseListByObject(String objectType, Long objectId);

    void baseDeleteByEnvCommandId(DevopsEnvCommandDTO devopsEnvCommandDTO);
}
