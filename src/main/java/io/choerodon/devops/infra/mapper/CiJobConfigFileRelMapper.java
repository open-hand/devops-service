package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiJobConfigFileRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * CI配置文件关联表(CiJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:03
 */
public interface CiJobConfigFileRelMapper extends BaseMapper<CiJobConfigFileRelDTO> {
    void deleteByJobIds(@Param("jobIds") List<Long> jobIds);

    List<CiJobConfigFileRelDTO> listByJobId(@Param("jobId") Long jobId);
}

