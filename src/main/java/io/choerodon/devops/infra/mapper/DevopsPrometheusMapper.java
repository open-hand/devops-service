package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: 25499
 * @date: 2019/10/28 15:55
 * @description:
 */
public interface DevopsPrometheusMapper extends Mapper<DevopsPrometheusDTO> {
    /**
     * 根据prometheusId查询Prometheus安装信息(包含相关pv信息)
     * @param id
     * @return
     */
    DevopsPrometheusVO queryPrometheusWithPvById(@Param("id") Long id);
}
