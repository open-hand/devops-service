package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:44
 */
public interface DevopsHostCommandMapper extends BaseMapper<DevopsHostCommandDTO> {
    DevopsHostCommandDTO selectLatestByInstanceIdAndType(@Param("instanceId") Long id, @Param("instanceType") String instanceType);

    void deleteByHostId(@Param("hostId") Long hostId);

    DevopsHostCommandDTO queryInstanceLatest(@Param("instanceId") Long instanceId, @Param("instanceType") String instanceType);

    DevopsHostCommandDTO queryDockerInstanceLatest(@Param("appId") Long appId, @Param("instanceType") String instanceType);

    void batchUpdateTimeoutCommand(@Param("missCommands") Set<Long> missCommands);

    List<DevopsHostCommandDTO> listStagnatedRecord(@Param("hostId") String hostId, @Param("beforeDate") String beforeDate);

    List<DevopsHostCommandDTO> listByIds(@Param("missCommands") Set<Long> missCommands);

    List<DevopsHostCommandDTO> listByTypeAndInsIds(@Param("insIds") Set<Long> insIds, @Param("instanceType") String instanceType);
}
