package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsPvPermissionUpateVO;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;

public interface DevopsPvServcie {

    PageInfo<DevopsPvVO> queryAll(PageRequest pageRequest);

    void createPv(DevopsPvDTO devopsPvDTO);

    void assignPermission(DevopsPvPermissionUpateVO update);
}
