package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 〈功能简述〉
 * 〈Ci流水线Mapper〉
 *
 * @author wanghao
 * @Date 2020/4/2 18:01
 */
public interface DevopsCiPipelineMapper extends Mapper<DevopsCiPipelineDTO> {

    List<DevopsCiPipelineVO> queryByProjectId(@Param("projectId") Long projectId);
}
