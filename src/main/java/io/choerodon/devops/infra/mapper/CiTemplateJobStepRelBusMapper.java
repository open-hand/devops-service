package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface CiTemplateJobStepRelBusMapper extends BaseMapper<CiTemplateJobStepRelDTO> {
    void deleteByJobId(Long jobId);

    List<CiTemplateJobStepRelDTO> selectNonVisibilityByJobIds(@Param("jobIds") Set<Long> jobIds);


    void deleteByIds(@Param("jobStepRelIds") Set<Long> jobStepRelIds);

}
