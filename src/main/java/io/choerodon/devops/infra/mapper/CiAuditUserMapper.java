package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiAuditUserDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * ci 人工卡点审核人员表(CiAuditUser)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:41
 */
public interface CiAuditUserMapper extends BaseMapper<CiAuditUserDTO> {

    void batchDeleteByConfigIds(@Param("configIds") List<Long> configIds);
}

