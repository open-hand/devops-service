package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.vuln.VulnerabilityVO;
import io.choerodon.devops.infra.dto.VulnTargetRelDTO;

/**
 * 漏洞扫描对象关系表(VulnTargetRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
public interface VulnTargetRelService {

    void batchSave(List<VulnTargetRelDTO> vulnTargetRelDTOList);

    List<VulnerabilityVO> listByTargetId(Long targetId);
}

