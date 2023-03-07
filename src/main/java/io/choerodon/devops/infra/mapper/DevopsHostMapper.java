package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsHostVO;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2020/9/15
 */
public interface DevopsHostMapper extends BaseMapper<DevopsHostDTO> {
    /**
     * 根据参数查询主机数据列表
     *
     * @param projectId   项目id
     * @param searchParam 查询参数
     * @return 主机列表
     */
    List<DevopsHostVO> listByOptions(@Param("projectId") Long projectId,
                                     @Param("searchParam") String searchParam);

    List<DevopsHostDTO> listByProjectIdAndIds(@Param("projectId") Long projectId,
                                              @Param("hostIds") Set<Long> hostIds);

    /**
     * 查询指定项目下的所有主机
     *
     * @param projectIds 项目ID列表
     * @return 主机
     */
    List<DevopsHostDTO> listByProject(@Param("projectIds") List<Long> projectIds);

    List<DevopsHostVO> listMemberHostByOptions(@Param("projectId") Long projectId,
                                               @Param("searchParam") String searchParam,
                                               @Param("userId") Long userId);

    List<CiCdPipelineDTO> selectPipelineByHostId(@Param("hostId") Long hostId);

}
