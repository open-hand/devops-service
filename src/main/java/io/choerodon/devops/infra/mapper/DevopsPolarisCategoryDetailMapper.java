package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsPolarisCategoryDetailDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface DevopsPolarisCategoryDetailMapper extends BaseMapper<DevopsPolarisCategoryDetailDTO> {
    /**
     * 根据id批量删除纪录
     *
     * @param detailIds id列表，不能为空
     */
    void batchDelete(@Param("ids") List<Long> detailIds);

    /**
     * 查询这次扫描对应的所有detail纪录id
     *
     * @param recordId 扫描纪录id
     * @return id列表
     */
    List<Long> queryDetailIdsByRecordId(@Param("recordId") Long recordId);
}
