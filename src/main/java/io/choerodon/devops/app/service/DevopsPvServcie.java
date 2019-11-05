package io.choerodon.devops.app.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsPvPermissionUpateVO;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;

public interface DevopsPvServcie {

    /***
     * 查询根据Id查询单个PV
     */


    /***
     * 根据条件分页查询PV
     */
    PageInfo<DevopsPvVO> basePagePvByOptions(Boolean doPage, PageRequest pageRequest, String params);

    /***
     * 创建PV
     * @param devopsPvDTO
     */
    void createPv(DevopsPvDTO devopsPvDTO);


    /***
     * 创建组织与PV的权限关联关系
     * @param update
     */
    void assignPermission(DevopsPvPermissionUpateVO update);

    /***
     * 更新PV表中的权限校验字段
     * @param update
     */
    void updateCheckPermission(DevopsPvPermissionUpateVO update);

    /**
     * 更新PV表字段
     */
    void updatePv(DevopsPvDTO devopsPvDTO);

    /**
     * 删除PV
     */
    void deletePvById(Long pvId);

}
