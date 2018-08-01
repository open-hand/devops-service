package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 16:17
 * Description:
 */
public interface DevopsEnvFileResourceRepository {

    DevopsEnvFileResourceE createFileResource(DevopsEnvFileResourceE devopsEnvFileResourceE);

    DevopsEnvFileResourceE getFileResource(Long fileResourceId);

    DevopsEnvFileResourceE updateFileResource(DevopsEnvFileResourceE devopsEnvFileResourceE);

    void deleteFileResource(Long fileResourceId);

    DevopsEnvFileResourceE queryByEnvIdAndResource(Long envId,Long resourceId,String resourceType);

    DevopsEnvFileResourceE queryByEnvIdAndPath(Long envId,String path);

    void deleteByEnvIdAndResource(Long envId,Long resourceId, String resourceType);

}
