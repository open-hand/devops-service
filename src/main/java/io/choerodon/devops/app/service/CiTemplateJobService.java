package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;

/**
 * 流水线任务模板表(CiTemplateJob)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:15
 */
public interface CiTemplateJobService {

    List<CiTemplateJobVO> listByStageIds(Set<Long> stageIds);

//    List<CiTemplateJobVO> listByStageIdWithGroupInfo(Long stageId);

    List<DevopsCiJobVO> listJobsByGroupId(Long projectId, Long groupId);
}

