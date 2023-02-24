package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTplJobConfigFileRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * CI任务模板配置文件关联表(CiTplJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:17
 */
public interface CiTplJobConfigFileRelMapper extends BaseMapper<CiTplJobConfigFileRelDTO> {
    List<CiTplJobConfigFileRelDTO> listByJobId(@Param("jobId") Long jobId);
}

