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


    /***
     * 创建组织与pv的权限关联关系
     * @param update
     */
    void assignPermission(DevopsPvPermissionUpateVO update);

    /***
     * 更新pv表中的权限校验字段
     * @param update
     */
    void updateCheckPermission(DevopsPvPermissionUpateVO update);

    /**
     * 更新pv表字段
     */
    void updatePv(DevopsPvDTO devopsPvDTO);
}
