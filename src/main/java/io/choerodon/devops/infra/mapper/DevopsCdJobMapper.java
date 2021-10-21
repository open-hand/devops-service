package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCdJobVO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCdJobMapper extends BaseMapper<DevopsCdJobDTO> {

    List<DevopsCdJobVO> listByIdsWithNames(@Param("jobIds") Set<Long> jobIds);
}
