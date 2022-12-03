//package io.choerodon.devops.app.service;
//
//import io.choerodon.core.domain.Page;
//import io.choerodon.devops.api.vo.pipeline.PipelineCompositeRecordVO;
//import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;
//import io.choerodon.mybatis.pagehelper.domain.PageRequest;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2020/7/14 20:52
// */
//public interface DevopsPipelineRecordRelService {
//
//    void save(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO);
//
//    DevopsPipelineRecordRelDTO queryByPipelineIdAndCiPipelineRecordId(Long pipelineId, Long ciPipelineRecordId);
//
//    void update(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO);
//
//    Page<DevopsPipelineRecordRelDTO> pagingPipelineRel(Long id, PageRequest cicdPipelineRel);
//
//    DevopsPipelineRecordRelDTO queryById(Long pipelineRecordRelId);
//
//    PipelineCompositeRecordVO queryLatestedPipelineRecord(Long id);
//
//    DevopsPipelineRecordRelDTO queryByCdPipelineRecordId(Long cdPipelineRecordId);
//}
