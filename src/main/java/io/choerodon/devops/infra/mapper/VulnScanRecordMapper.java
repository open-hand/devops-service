package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.vuln.VulnTargetVO;
import io.choerodon.devops.infra.dto.VulnScanRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 漏洞扫描记录表(VulnScanRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
public interface VulnScanRecordMapper extends BaseMapper<VulnScanRecordDTO> {
    List<VulnTargetVO> queryDetailsById(Long recordId);
}

