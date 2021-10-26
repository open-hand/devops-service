package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsDaemonSetVO;
import io.choerodon.devops.infra.dto.DevopsDaemonSetDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:14
 */
public interface DevopsDaemonSetMapper extends BaseMapper<DevopsDaemonSetDTO>,WorkLoadBaseMapper {

    List<DevopsDaemonSetVO> listByEnvId(@Param("envId") Long envId,
                                        @Param("name") String name,
                                        @Param("fromInstance") Boolean fromInstance);

}
