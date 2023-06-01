package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.CiPipelineVlunScanRecordRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * ci流水线漏洞扫描记录关系表(CiPipelineVlunScanRecordRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:24
 */
public interface CiPipelineVlunScanRecordRelMapper extends BaseMapper<CiPipelineVlunScanRecordRelDTO> {
    void baseCreate(CiPipelineVlunScanRecordRelDTO ciPipelineVlunScanRecordRelDTO);
}

