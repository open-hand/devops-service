package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 16:17
 * Description:
 */
public interface DevopsEnvFileResourceRepository {

    DevopsEnvFileResourceVO baseCreate(DevopsEnvFileResourceVO devopsEnvFileResourceE);

    DevopsEnvFileResourceVO baseQuery(Long fileResourceId);

    DevopsEnvFileResourceVO baseUpdate(DevopsEnvFileResourceVO devopsEnvFileResourceE);

    void baseDelete(Long fileResourceId);

    DevopsEnvFileResourceVO baseQueryByEnvIdAndResourceId(Long envId, Long resourceId, String resourceType);

    List<DevopsEnvFileResourceVO> baseQueryByEnvIdAndPath(Long envId, String path);

    void baseDeleteByEnvIdAndResourceId(Long envId, Long resourceId, String resourceType);

}
