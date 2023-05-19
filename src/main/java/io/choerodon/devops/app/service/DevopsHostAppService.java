package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;
import java.util.Set;

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
     *
     * @param projectId
     * @param jarDeployVO
     */
    void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO);

    String calculateSourceConfig(JarDeployVO jarDeployVO);

    /**
     * 查询java实例列表
     *
     * @param hostId
     * @return
     */
    List<DevopsHostAppDTO> listByHostId(Long hostId);

    void baseUpdate(DevopsHostAppDTO devopsHostAppDTO);

    void baseDelete(Long id);

    DevopsHostAppDTO baseQuery(Long id);

    DevopsHostAppDTO queryByHostIdAndCode(Long hostId, String name);

    Page<DevopsHostAppVO> pagingAppByHost(Long projectId, Long hostId, PageRequest pageRequest, String rdupmType, String operationType, String params, String name, Long appId);

    /**
     * 查询主机下的应用实例详情
     *
     * @param projectId
     * @param id
     * @return DevopsHostAppVO
     */
    DevopsHostAppVO queryAppById(Long projectId, Long id);


    void checkNameAndCodeUniqueAndThrow(Long projectId, Long hostId, Long appId, String name, String code);

    void checkNameUniqueAndThrow(Long projectId, Long hostId, Long appId, String name);

    void checkCodeUniqueAndThrow(Long projectId, Long hostId, Long appId, String code);

    Boolean checkNameUnique(Long projectId, Long hostId, Long appId, String name);

    Boolean checkCodeUnique(Long projectId, Long hostId, Long appId, String code);

    /**
     * 删除主机应用
     *
     * @param projectId
     * @param appId
     * @param hostId
     */
    void deleteById(Long projectId, Long hostId, Long appId);

    void baseCreate(DevopsHostAppDTO devopsHostAppDTO, String errorCode);

    void deployCustomInstance(Long projectId, CustomDeployVO customDeployVO);

    List<PipelineInstanceReferenceVO> queryPipelineReferenceHostApp(Long projectId, Long appId);

    void restart(Long projectId, Long hostId, Long appId);

    Set<String> listWorkDirs(Long projectId, Long hostId);
}
