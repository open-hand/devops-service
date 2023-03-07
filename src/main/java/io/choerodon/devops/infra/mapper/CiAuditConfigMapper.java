package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiAuditConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * ci 人工卡点配置表(CiAuditConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:27
 */
public interface CiAuditConfigMapper extends BaseMapper<CiAuditConfigDTO> {

    List<CiAuditConfigDTO> listByStepIds(@Param("stepIds") Set<Long> stepIds);

    void batchDeleteByIds(@Param("ids") List<Long> ids);
}

