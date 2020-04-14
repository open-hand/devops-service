package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsDeployRecordInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2/26/20
 */
public interface DevopsDeployRecordInstanceMapper extends BaseMapper<DevopsDeployRecordInstanceDTO> {
    void batchInsert(@Param("items") List<DevopsDeployRecordInstanceDTO> items);

    /**
     * 通过部署纪录批量删除此表纪录
     *
     * @param recordIds 部署纪录id集合，不能为空
     */
    void deleteRecordInstanceByRecordIds(@Param("ids") List<Long> recordIds);
}
