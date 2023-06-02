package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.vuln.VulnerabilityVO;
import io.choerodon.devops.infra.dto.VulnTargetRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 漏洞扫描对象关系表(VulnTargetRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
public interface VulnTargetRelMapper extends BaseMapper<VulnTargetRelDTO> {
    List<VulnerabilityVO> listByTargetId(@Param("targetId") Long targetId,
                                         @Param("pkgName") String pkgName,
                                         @Param("severity") String severity,
                                         @Param("param") String param);
}

