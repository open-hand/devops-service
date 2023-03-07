package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiTemplateJobBusMapper extends BaseMapper<CiTemplateJobDTO> {
    Integer isNameUnique(@Param("name") String name, @Param("sourceId") Long sourceId, @Param("jobId") Long jobId);

    List<CiTemplateJobVO> pageUnderOrgLevel(@Param("sourceId") Long sourceId,
                                            @Param("sourceType") String sourceType,
                                            @Param("organizationId") Long organizationId,
                                            @Param("name") String name,
                                            @Param("groupName") String groupName,
                                            @Param("groupId") Long groupId,
                                            @Param("builtIn") Boolean builtIn,
                                            @Param("params") String params);

    List<CiTemplateJobDTO> queryJobByStageId(@Param("stageId") Long stageId);

    List<CiTemplateJobVO> queryAllCiTemplateJob(@Param("sourceId") Long sourceId,
                                                @Param("sourceType") String sourceType,
                                                @Param("organizationId") Long organizationId);

    List<CiTemplateJobDTO> selectNonVisibilityJob(@Param("stageId") Long stageId);

    void deleteNonVisibilityJobByIds(@Param("jobIds") Set<Long> jobIds);

    List<CiTemplateJobVO> listByIds(@Param("jobIds") Set<Long> jobIds);

    Integer existRecord(@Param("sourceType") String sourceType, @Param("templateJobId") Long templateJobId);


}

