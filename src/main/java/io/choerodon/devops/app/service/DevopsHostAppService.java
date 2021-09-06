package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsHostAppService {
    /**
     * 部署java应用
     * @param projectId
     * @param jarDeployVO
     */
    void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO);

    String calculateSourceConfig(JarDeployVO jarDeployVO);
    /**
     * 查询java实例列表
     * @param hostId
     * @return
     */
    List<DevopsHostAppDTO> listByHostId(Long hostId);

    void baseUpdate(DevopsHostAppDTO devopsHostAppDTO);

    void baseDelete(Long instanceId);

    DevopsHostAppDTO baseQuery(Long instanceId);

    DevopsHostAppDTO queryByHostIdAndCode(Long hostId, String name);

    Page<DevopsHostAppVO> pagingAppByHost(Long projectId, Long hostId, PageRequest pageRequest, String rdupmType, String operationType, String params);

    /**
     * 查询主机下的应用实例详情
     * @param projectId
     * @param id
     * @return DevopsHostAppVO
     */
    DevopsHostAppVO queryAppById(Long projectId, Long id);

    /**
     * 删除主机应用
     * @param projectId
     * @param appId
     * @param hostId
     */
    void deleteById(Long projectId, Long hostId, Long appId);

    void baseCreate(DevopsHostAppDTO devopsHostAppDTO, String errorCode);
}
