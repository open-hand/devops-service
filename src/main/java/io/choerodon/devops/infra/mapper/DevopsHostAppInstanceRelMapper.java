package io.choerodon.devops.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.AppServiceRepVO;
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
    List<AppServiceRepVO> selectHostAppByProjectId(@Param("projectId") Long projectId, @Param("type") String type, @Param("hostId") Long hostId);

    List<DevopsHostAppInstanceRelDTO> queryInstanceListByHostId(@Param("projectId") Long projectId, @Param("hostId") Long hostId);
}
