package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线任务模板表(CiTemplateJob)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:15
 */
public interface CiTemplateJobMapper extends BaseMapper<CiTemplateJobDTO> {

    List<CiTemplateJobDTO> listByStageId(Long stageId);

    List<CiTemplateJobVO> listByStageIds(@Param("stageIds") Set<Long> stageIds);

    List<CiTemplateJobDTO> listByTenantIdAndGroupId(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId, @Param("groupId") Long groupId);

    void updateImageByIds(@Param("ids") Set<Long> jobIds, @Param("image") String sonarImage);

    List<CiTemplateJobVO> listConfigFileReferenceInfo(@Param("configFileId") Long configFileId);
}

