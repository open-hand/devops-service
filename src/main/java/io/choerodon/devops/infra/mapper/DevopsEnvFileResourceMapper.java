package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 15:42
 * Description:
 */
public interface DevopsEnvFileResourceMapper extends BaseMapper<DevopsEnvFileResourceDTO> {
    /**
     * 统计解析纪录
     *
     * @param envId        环境id
     * @param resourceType 资源类型
     * @param resourceId   资源id
     * @return 符合条件的纪录的个数
     */
    int countRecords(@Param("envId") Long envId,
                     @Param("resourceType") String resourceType,
                     @Param("resourceId") Long resourceId);
}
