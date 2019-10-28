package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:14 2019/7/15
 * Description:
 */
public interface DevopsEnvResourceDetailService {
    DevopsEnvResourceDetailDTO baseCreate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO);

    DevopsEnvResourceDetailDTO baesQueryByMessageId(Long messageId);

    void baseUpdate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO);

    /**
     * 批量查询 DevopsEnvResourceDetailDTO 根据 resourceDetailIds
     * @param resourceDetailIds
     * @return
     */
    List<DevopsEnvResourceDetailDTO> listByMessageIds(Set<Long> resourceDetailIds);
}
