package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsJenkinsServerVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsJenkinsServerMapper extends BaseMapper<DevopsJenkinsServerDTO> {
    Boolean checkNameExist(@Param("projectId") Long projectId, @Param("jenkinsServerId") Long jenkinsServerId, @Param("serverName") String serverName);

    List<DevopsJenkinsServerVO> page(@Param("projectId") Long projectId, @Param("searchVO") SearchVO searchVO);
}
