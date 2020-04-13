package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import org.springframework.data.domain.Pageable;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:26
 */
public interface DevopsCiPipelineRecordService {
    void create(PipelineWebHookVO pipelineWebHookVO, String token);

    void handleCreate(PipelineWebHookVO pipelineWebHookVO);

    PageInfo<DevopsCiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, Pageable pageable);

    DevopsCiPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId);

    void deleteByPipelineId(Long ciPipelineId);

    List<DevopsCiPipelineRecordDTO> queryByPipelineId(Long ciPipelineId);
}
