package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:20
 */
public interface DevopsCiJobMapper extends BaseMapper<DevopsCiJobDTO> {
    List<DevopsCiJobDTO> listOldDataByType(String type);

    List<DevopsCiJobVO> listCustomByPipelineId(@Param("ciPipelineId") Long ciPipelineId);

    void updateImageByIds(@Param("ids") List<Long> longList, @Param("image") String sonarImage);

}
