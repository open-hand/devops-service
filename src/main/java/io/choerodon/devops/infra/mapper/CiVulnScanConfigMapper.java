package io.choerodon.devops.infra.mapper;

import java.util.Set;

import io.choerodon.devops.infra.dto.CiVulnScanConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * ci 漏洞扫描配置信息表(CiVulnScanConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:11
 */
public interface CiVulnScanConfigMapper extends BaseMapper<CiVulnScanConfigDTO> {
    void batchDeleteByStepIds(Set<Long> stepIds);
}
