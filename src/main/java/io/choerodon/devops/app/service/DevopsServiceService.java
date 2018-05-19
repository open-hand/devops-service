package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsServiceDTO;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/13.
 */
public interface DevopsServiceService {

    /**
     * 部署网络
     *
     * @param projectId           项目id
     * @param devopsServiceReqDTO 部署网络参数
     * @return Boolean
     */
    Boolean insertDevopsService(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO);

    /**
     * 更新网络
     *
     * @param projectId           项目id
     * @param devopsServiceReqDTO 部署网络参数
     * @return
     */
    Boolean updateDevopsService(Long projectId, Long id, DevopsServiceReqDTO devopsServiceReqDTO);

    /**
     * 删除网络
     *
     * @param id        网络ID
     * @return
     */
    void deleteDevopsService(Long id);

    /**
     * 检查网络唯一性
     *
     * @param name 网络名
     * @return Boolean
     */
    Boolean checkName(Long projectId, Long envId, String name);

    /**
     * 分页查询网络列表
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceDTO
     */
    Page<DevopsServiceDTO> listDevopsServiceByPage(Long projectId, PageRequest pageRequest, String searchParam);

    /**
     * 分页查询网络列表
     *
     * @param envId     参数
     * @return List of DevopsServiceDTO
     */
    List<DevopsServiceDTO> listDevopsService(Long envId);

    /**
     * 查询单个网络
     *
     * @param id        网络id
     * @return DevopsServiceDTO
     */
    DevopsServiceDTO query(Long id);
}
