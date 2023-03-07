package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.CiPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:21
 */
public interface DevopsCiPipelineRecordMapper extends BaseMapper<DevopsCiPipelineRecordDTO> {

    /**
     * 查询流水线执行记录
     *
     * @param ciPipelineId
     * @return
     */
    List<CiPipelineRecordVO> listByCiPipelineId(@Param("ciPipelineId") Long ciPipelineId);

    /**
     * 查询猪齿鱼流水线对应的所有流水线的纪录的gitlab流水线纪录id
     *
     * @param ciPipelineId 猪齿鱼流水线id
     * @return gitlab流水线纪录id列表
     */
    List<Long> listGitlabPipelineIdsByPipelineId(@Param("ciPipelineId") Long ciPipelineId);

    int updateStatusByGitlabPipelineId(@Param("gitlabPipelineId") Long gitlabPipelineId, @Param("status") String status);

    /**
     * 根据流水线纪录id查询应用服务
     *
     * @param recordId 流水线纪录id
     * @return 应用服务
     */
    AppServiceDTO queryGitlabProjectIdByRecordId(@Param("pipelineRecordId") Long recordId);

    List<DevopsCiPipelineRecordDTO> queryNotSynchronizedRecord(@Param("date") Date date);

    DevopsCiPipelineRecordVO selectById(Long ciPipelineRecordId);

    DevopsCiPipelineRecordDTO queryByAppServiceIdAndGitlabPipelineId(@Param("appServiceId") Long appServiceId, @Param("gitlabPipelineId") Long gitlabPipelineId);

    DevopsCiPipelineRecordDTO queryByIdWithPipelineName(@Param("ciPipelineRecordId") Long ciPipelineRecordId);

    List<DevopsCiPipelineRecordDTO> listByPipelineId(@Param("pipelineId") Long pipelineId,
                                                     @Param("startTime") java.sql.Date startTime,
                                                     @Param("endTime") java.sql.Date endTime);

    List<DevopsCiPipelineRecordDTO> listByPipelineIds(@Param("pipelineIds") List<Long> pipelineIds,
                                                      @Param("startTime") java.sql.Date startTime,
                                                      @Param("endTime") java.sql.Date endTime);
}
