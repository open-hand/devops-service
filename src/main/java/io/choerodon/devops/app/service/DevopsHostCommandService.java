package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:45
 */
public interface DevopsHostCommandService {

    void baseCreate(DevopsHostCommandDTO devopsHostCommandDTO);

    DevopsHostCommandDTO baseQueryById(Long commandId);

    void baseUpdate(DevopsHostCommandDTO devopsHostCommandDTO);

    DevopsHostCommandDTO queryInstanceLatest(Long instanceId);

    /**
     * 查询出处于操作中状态三分钟及以上的记录
     * @param hostId
     * @return
     */
    List<DevopsHostCommandDTO> listStagnatedRecord(String hostId);

    /**
     * 处理超时命令
     * @param missCommands
     */
    void batchUpdateTimeoutCommand(Set<Long> missCommands);
}
