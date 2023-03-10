package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.ConfigFileVO;
import io.choerodon.devops.infra.dto.ConfigFileDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 配置文件表(ConfigFile)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:06
 */
public interface ConfigFileMapper extends BaseMapper<ConfigFileDTO> {
    List<ConfigFileVO> listByParams(@Param("sourceType") String sourceType,
                                    @Param("sourceId") Long sourceId,
                                    @Param("name") String name,
                                    @Param("description") String description,
                                    @Param("params") String params);

    ConfigFileVO queryByIdWithDetail(@Param("id") Long id);
}

