package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<DevopsHostAppVO> listByOptions(@Param("projectId") Long projectId,
                                        @Param("hostId") Long hostId,
                                        @Param("rdupmType") String rdupmType,
                                        @Param("operationType") String operationType,
                                        @Param("params") String params);

    List<DevopsHostAppVO> listBasicInfoByOptions(@Param("projectId") Long projectId,
                                                 @Param("hostId") Long hostId,
                                                 @Param("rdupmType") String rdupmType,
                                                 @Param("operationType") String operationType,
                                                 @Param("params") String params,
                                                 @Param("name") String name,
                                                 @Param("appId") Long appId);

    /**
     * 查询主机下的应用实例详情
     *
     * @param id
     * @return DevopsHostAppVO
     */
    DevopsHostAppVO queryAppById(@Param("id") Long id);

    List<DevopsHostAppVO> listOwnedByOptions(@Param("projectId") Long projectId,
                                             @Param("userId") Long userId,
                                             @Param("hostId") Long hostId,
                                             @Param("rdupmType") String rdupmType,
                                             @Param("operationType") String operationType,
                                             @Param("params") String params);

    List<DevopsHostAppVO> listOwnedBasicInfoByOptions(@Param("projectId") Long projectId,
                                                      @Param("userId") Long userId,
                                                      @Param("hostId") Long hostId,
                                                      @Param("rdupmType") String rdupmType,
                                                      @Param("operationType") String operationType,
                                                      @Param("params") String params,
                                                      @Param("name") String name,
                                                      @Param("appId") Long appId);

    Boolean checkNameUnique(@Param("projectId") Long projectId,
                            @Param("hostId") Long hostId,
                            @Param("appId") Long appId,
                            @Param("name") String name);

    Boolean checkCodeUnique(@Param("projectId") Long projectId,
                            @Param("hostId") Long hostId,
                            @Param("appId") Long appId,
                            @Param("code") String code);

    List<DevopsHostAppVO> listWorkDirsByHostId(@Param("hostId") Long hostId);
}
