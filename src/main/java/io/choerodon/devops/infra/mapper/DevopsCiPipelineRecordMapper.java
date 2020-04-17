package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:21
 */
public interface DevopsCiPipelineRecordMapper extends Mapper<DevopsCiPipelineRecordDTO>{

    /**
     * 查询流水线执行记录
     * @param ciPipelineId
     * @return
     */
    List<DevopsCiPipelineRecordVO> listByCiPipelineId(@Param("ciPipelineId") Long ciPipelineId);

    int updateStatusByGitlabPipelineId(@Param("gitlabPipelineId") Long gitlabPipelineId, @Param("status") String status);
}
