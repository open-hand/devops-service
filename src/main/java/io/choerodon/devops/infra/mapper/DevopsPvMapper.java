package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface DevopsPvMapper extends Mapper<DevopsPvDTO> {

    List<DevopsPvVO> queryAll();
}
