package io.choerodon.devops.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.AppServiceRepVO;
import io.choerodon.devops.api.vo.host.DevopsHostInstanceVO;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/14 10:19
 */
public interface DevopsHostAppInstanceRelMapper extends BaseMapper<DevopsHostAppInstanceRelDTO> {
    List<AppServiceRepVO> selectHostAppByProjectId(@Param("projectId") Long projectId, @Param("type") String type, @Param("hostId") Long hostId, @Param("params") String params);

    List<DevopsHostAppInstanceRelDTO> queryInstanceListByHostIdAndAppId(@Param("projectId") Long projectId, @Param("hostId") Long hostId, @Param("appId") Long appId, @Param("name") String name, @Param("type") String type, @Param("status") String status, @Param("params") String params);

    List<DevopsHostInstanceVO> queryInstanceListByHostId(@Param("hostId") Long hostId, @Param("name") String name, @Param("type") String type, @Param("status") String status, @Param("params") String params);
}
