package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsHostCreateRequestVO;
import io.choerodon.devops.api.vo.DevopsHostUpdateRequestVO;
import io.choerodon.devops.api.vo.DevopsHostVO;

/**
 * @author zmf
 * @since 2020/9/15
 */
public interface DevopsHostService {
    /**
     * 创建主机
     *
     * @param projectId                 项目id
     * @param devopsHostCreateRequestVO 主机相关数据
     * @return 创建后的主机
     */
    DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO);

    /**
     * 更新主机
     *
     * @param projectId                 项目id
     * @param hostId                    主机id
     * @param devopsHostUpdateRequestVO 主机更新相关数据
     * @return 更新后的主机
     */
    DevopsHostVO updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO);

    /**
     * 查询主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     */
    DevopsHostVO queryHost(Long projectId, Long hostId);

    /**
     * 删除主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     */
    void deleteHost(Long projectId, Long hostId);
}
