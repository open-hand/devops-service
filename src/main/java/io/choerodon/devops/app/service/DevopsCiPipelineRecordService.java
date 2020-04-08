package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
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
}
