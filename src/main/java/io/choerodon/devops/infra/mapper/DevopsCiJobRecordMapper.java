package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCiJobRecordVO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:22
 */
public interface DevopsCiJobRecordMapper extends Mapper<DevopsCiJobRecordDTO> {

    List<DevopsCiJobRecordVO> batchListByCiPipelineRecordId(@Param("pids") Set<Long> pids);
}
