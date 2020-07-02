package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiCdStageRecordDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:07
 */
public interface CiCdStageRecordService {

    List<CiCdStageRecordDTO> queryByPipelineRecordId(Long pipelineRecordId);
}
