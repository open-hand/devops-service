package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsStatefulSetVO;
import io.choerodon.devops.infra.dto.DevopsStatefulSetDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:15
 */
public interface DevopsStatefulSetMapper extends BaseMapper<DevopsStatefulSetDTO>, WorkLoadBaseMapper {

    List<DevopsStatefulSetVO> listByEnvId(@Param("envId") Long envId, @Param("name") String name, @Param("fromInstance") Boolean fromInstance);
}
