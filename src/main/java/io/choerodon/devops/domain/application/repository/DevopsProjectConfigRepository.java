package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectConfigE;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;

import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigRepository {

    DevopsProjectConfigDTO baseCreate(DevopsProjectConfigDTO devopsProjectConfigDTO);

    Boolean baseCheckByName(DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO baseUpdate(DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO baseQuery(Long id);

    DevopsProjectConfigDTO baseQueryByName(Long projectId, String name);

    DevopsProjectConfigDTO baseQueryByNameWithNullProject(String name);

    PageInfo<DevopsProjectConfigDTO> basePageByOptions(Long projectId, PageRequest pageRequest, String params);

    void baseDelete(Long id);

    List<DevopsProjectConfigDTO> baseListByIdAndType(Long projectId, String type);

    void baseCheckByName(Long projectId, String name);

    Boolean baseCheckUsed(Long checkIsUsed);
}
