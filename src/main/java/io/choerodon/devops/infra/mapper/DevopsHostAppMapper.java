package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsHostAppMapper extends BaseMapper<DevopsHostAppDTO> {

    List<DevopsHostAppDTO> listByHostId(Long hostId);

    void deleteByHostId(@Param("hostId") Long hostId);

    List<DevopsHostAppVO> listByOptions(@Param("hostId") Long hostId,
                                        @Param("rdupmType") String rdupmType,
                                        @Param("operationType") String operationType,
                                        @Param("params") String params);

    /**
     * 查询主机下的应用实例详情
     * @param id
     * @return DevopsHostAppVO
     */
    DevopsHostAppVO queryAppById(@Param("id") Long id);

    List<DevopsHostAppVO> listOwnedByOptions(@Param("userId") Long userId,
                                             @Param("hostId") Long hostId,
                                             @Param("rdupmType") String rdupmType,
                                             @Param("operationType") String operationType,
                                             @Param("params") String params);
}
