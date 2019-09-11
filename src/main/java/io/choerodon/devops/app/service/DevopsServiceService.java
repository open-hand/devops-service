package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.app.eventhandler.payload.ServiceSagaPayLoad;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;

/**
 * Created by Zenger on 2018/4/13.
 */
public interface DevopsServiceService {

    /**
     * 部署网络
     *
     * @param projectId          项目id
     * @param devopsServiceReqVO 部署网络参数
     * @return Boolean
     */
    Boolean create(Long projectId, DevopsServiceReqVO devopsServiceReqVO);


    /**
     * 部署网络,GitOps
     *
     * @param projectId          项目id
     * @param devopsServiceReqVO 部署网络参数
     * @return Boolean
     */
    Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqVO devopsServiceReqVO, Long userId);

    /**
     * 更新网络
     *
     * @param projectId          项目id
     * @param id                 网络Id
     * @param devopsServiceReqVO 部署网络参数
     * @return boolean
     */
    Boolean update(Long projectId, Long id, DevopsServiceReqVO devopsServiceReqVO);

    /**
     * 更新网络
     *
     * @param projectId          项目id
     * @param id                 网络Id
     * @param devopsServiceReqVO 部署网络参数
     * @return boolean
     */
    Boolean updateDevopsServiceByGitOps(Long projectId, Long id, DevopsServiceReqVO devopsServiceReqVO, Long userId);


    /**
     * 删除网络
     *
     * @param id 网络ID
     */
    void delete(Long id);


    /**
     * 删除网络
     *
     * @param id 网络ID
     */
    void deleteDevopsServiceByGitOps(Long id);

    /**
     * 检查网络唯一性
     *
     * @param envId 环境Id
     * @param name  网络名
     * @return Boolean
     */
    Boolean checkName(Long envId, String name);


    /**
     * 分页查询网络列表
     *
     * @param envId 参数
     * @return List of DevopsServiceVO
     */
    List<DevopsServiceVO> listByEnvId(Long envId);

    /**
     * 查询单个网络
     *
     * @param id 网络id
     * @return DevopsServiceVO
     */
    DevopsServiceVO query(Long id);

    /**
     * 分页查询网络（包含网络对应域名）
     *
     * @param projectId    项目id
     * @param envId        环境id
     * @param pageRequest  分页参数
     * @param searchParam  查询参数
     * @param appServiceId 应用服务id（如果有就是查询应用下的网络域名）
     * @return Page of DevopsServiceVO
     */
    PageInfo<DevopsServiceVO> pageByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam, Long appServiceId);


    /**
     * 根据网络名查询网络
     *
     * @param envId       网络id
     * @param serviceName 网络名
     * @return DevopsServiceVO
     */
    DevopsServiceVO queryByName(Long envId, String serviceName);

    /**
     * 查询实例下关联的网络域名（不包含chart）
     *
     * @param projectId    项目id
     * @param envId        环境id
     * @param instanceId   实例Id
     * @param pageRequest  分页参数
     * @param appServiceId 应用id
     * @param searchParam  查询参数
     * @return Page of DevopsServiceVO
     */
    PageInfo<DevopsServiceVO> pageByInstance(Long projectId, Long envId, Long instanceId, PageRequest pageRequest, Long appServiceId, String searchParam);

    /**
     * 查看网络信息时，展示网络对应实例的Pod实时数据
     *
     * @param id
     * @return
     */
    DevopsServiceVO querySingleService(Long id);

    void createServiceBySaga(ServiceSagaPayLoad serviceSagaPayLoad);

    DevopsServiceDTO baseQuery(Long id);

    Boolean baseCheckName(Long envId, String name);

    PageInfo<DevopsServiceQueryDTO> basePageByOptions(Long projectId, Long envId, Long instanceId, PageRequest pageRequest,
                                                      String searchParam, Long appServiceId);

    List<DevopsServiceQueryDTO> baseListRunningService(Long envId);

    List<DevopsServiceDTO> baseListByEnvId(Long envId);

    DevopsServiceDTO baseCreate(DevopsServiceDTO devopsServiceDTO);

    void baseDelete(Long id);

    void baseUpdate(DevopsServiceDTO devopsServiceDTO);

    void baseUpdateLabels(Long id);

    void baseUpdateEndPoint(Long id);

    List<Long> baseListEnvByRunningService();

    DevopsServiceDTO baseQueryByNameAndEnvId(String name, Long envId);

    Boolean baseCheckServiceByEnv(Long envId);


    List<DevopsServiceDTO> baseList();

    void baseDeleteServiceAndInstanceByEnvId(Long envId);

    void updateStatus(DevopsServiceDTO devopsServiceDTO);
}
