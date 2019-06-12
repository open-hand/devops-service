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
     * 部署网络,GitOps
     *
     * @param projectId           项目id
     * @param devopsServiceReqDTO 部署网络参数
     * @return Boolean
     */
    Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO, Long userId);

    /**
     * 更新网络
     *
     * @param projectId           项目id
     * @param id                  网络Id
     * @param devopsServiceReqDTO 部署网络参数
     * @return boolean
     */
    Boolean updateDevopsService(Long projectId, Long id, DevopsServiceReqDTO devopsServiceReqDTO);

    /**
     * 更新网络
     *
     * @param projectId           项目id
     * @param id                  网络Id
     * @param devopsServiceReqDTO 部署网络参数
     * @return boolean
     */
    Boolean updateDevopsServiceByGitOps(Long projectId, Long id, DevopsServiceReqDTO devopsServiceReqDTO, Long userId);


    /**
     * 删除网络
     *
     * @param id 网络ID
     */
    void deleteDevopsService(Long id);


    /**
     * 删除网络
     *
     * @param id 网络ID
     */
    void deleteDevopsServiceByGitOps(Long id);

    /**
     * 检查网络唯一性
     *
     * @param envId     环境Id
     * @param name      网络名
     * @return Boolean
     */
    Boolean checkName(Long envId, String name);


    /**
     * 分页查询网络列表
     *
     * @param envId 参数
     * @return List of DevopsServiceDTO
     */
    List<DevopsServiceDTO> listDevopsService(Long envId);

    /**
     * 查询单个网络
     *
     * @param id 网络id
     * @return DevopsServiceDTO
     */
    DevopsServiceDTO query(Long id);

    /**
     * /**
     * 环境总览网络查询
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceDTO
     */
    Page<DevopsServiceDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam);


    /**
     * 根据网络名查询网络
     *
     * @param envId   网络id
     * @param serviceName  网络名
     * @return DevopsServiceDTO
     */
    DevopsServiceDTO queryByName(Long envId, String serviceName);

    /**
     * 查询实例下关联的网络域名（不包含chart）
     *
     * @param projectId   项目id
     * @param instanceId  实例Id
     * @param pageRequest 分页参数
     * @return Page of DevopsServiceDTO
     */
    Page<DevopsServiceDTO> listByInstanceId(Long projectId, Long instanceId, PageRequest pageRequest);


}
