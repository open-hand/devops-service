package io.choerodon.devops.app.service;

/**
 * @author zmf
 * @since 2020/9/15
 */
public interface DevopsHostService {
    /**
     * 删除主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     */
    void deleteHost(Long projectId, Long hostId);
}
