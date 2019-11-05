package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface DevopsPvMapper extends Mapper<DevopsPvDTO> {

    List<DevopsPvVO> listPvByOptions(@Param("searchParam") Map<String,Object> searchParamMap,
                                     @Param("params") List<String> params);

    /**
     * 和cluster表和pvc表做连接查询获取name
     * @return
     */
    DevopsPvVO queryById(@Param("pvId")Long pvId);
}
