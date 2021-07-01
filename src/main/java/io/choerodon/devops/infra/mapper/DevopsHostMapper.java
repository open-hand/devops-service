package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsHostVO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
     * @param hostStatus  主机状态
     * @return 主机列表
     */
    List<DevopsHostVO> listByOptions(@Param("projectId") Long projectId,
                                      @Param("searchParam") String searchParam,
                                      @Param("hostStatus") String hostStatus);

    List<DevopsHostDTO> listByProjectIdAndIds(@Param("projectId") Long projectId,
                                              @Param("hostIds") Set<Long> hostIds);

    /**
     * 批量设置主机状态为处理中
     *
     * @param projectId 项目id
     * @param hostIds   主机id数据
     */
    void batchSetStatusOperating(@Param("projectId") Long projectId,
                                 @Param("hostIds") Set<Long> hostIds,
                                 @Param("date") Date date,
                                 @Param("updatedBy") Long updatedBy);

    /**
     * 批量设置主机状态为超时失败
     *
     * @param projectId 项目id
     * @param hostIds   主机id数据
     */
    void batchSetStatusTimeoutFailed(@Param("projectId") Long projectId,
                                     @Param("hostIds") Set<Long> hostIds,
                                     @Param("isTestType") Boolean isTestType,
                                     @Param("date") Date date);

    List<DevopsHostVO> listBySearchParam(@Param("projectId") Long projectId,
                                         @Param("searchParam") String searchParam);

    List<DevopsHostVO> pagingWithCheckingStatus(@Param("projectId") Long projectId,
                                                @Param("finalHostIds") Set<Long> finalHostIds,
                                                @Param("searchParam") String searchParam);
}
