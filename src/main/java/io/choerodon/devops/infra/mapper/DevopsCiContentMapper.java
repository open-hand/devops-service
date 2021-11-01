package io.choerodon.devops.infra.mapper;

import java.util.Set;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiContentDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:21
 */
public interface DevopsCiContentMapper extends BaseMapper<DevopsCiContentDTO> {

    String queryLatestContent(@Param("pipelineId") Long pipelineId);

    void updateCiContent(@Param("ids") Set<Long> ids, @Param("nexusDefaultUrl") String nexusDefaultUrl, @Param("nexusProxyUrl") String nexusProxyUrl);


}
