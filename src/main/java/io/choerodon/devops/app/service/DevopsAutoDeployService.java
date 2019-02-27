package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsAutoDeployDTO;
import io.choerodon.devops.api.dto.DevopsAutoDeployRecordDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:32 2019/2/25
 * Description:
 */
public interface DevopsAutoDeployService {
    /**
     * 项目下创建自动部署
     *
     * @param projectId           项目Id
     * @param devopsAutoDeployDTO 自动部署信息
     * @return DevopsAutoDeployDTO
     */
    DevopsAutoDeployDTO createOrUpdate(Long projectId, DevopsAutoDeployDTO devopsAutoDeployDTO);


    /**
     * 项目下删除自动部署
     *
     * @param projectId    项目id
     * @param autoDeployId 自动部署Id
     */
    void delete(Long projectId, Long autoDeployId);

    /**
     * 项目下查询自动部署信息
     *
     * @param projectId   项目Id
     * @param appId       应用Id
     * @param envId       环境Id
     * @param doPage      是否分页
     * @param pageRequest 分页信息
     * @param params      查询参数
     * @return
     */
    Page<DevopsAutoDeployDTO> listByOptions(Long projectId,
                                            Long appId,
                                            Long envId,
                                            Boolean doPage,
                                            PageRequest pageRequest,
                                            String params);

    /**
     * 项目下查询自动部署信息
     *
     * @param projectId   项目Id
     * @param appId       应用Id
     * @param envId       环境Id
     * @param taskName    任务名称
     * @param doPage      是否分页
     * @param pageRequest 分页信息
     * @param params      查询参数
     * @return
     */
    Page<DevopsAutoDeployRecordDTO> queryRecords(Long projectId,
                                                 Long appId,
                                                 Long envId,
                                                 String taskName,
                                                 Boolean doPage,
                                                 PageRequest pageRequest,
                                                 String params);


}
