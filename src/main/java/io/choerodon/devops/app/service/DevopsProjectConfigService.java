package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigService {
    DevopsProjectConfigDTO create(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO updateByPrimaryKeySelective(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO queryByPrimaryKey(Long id);

    Page<DevopsProjectConfigDTO> listByOptions(Long projectId, PageRequest pageRequest,String params);

    void delete(Long id);

    List<DevopsProjectConfigDTO> queryByIdAndType(Long projectId, String type);
}
