package io.choerodon.devops.infra.mapper;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.BaseApplicationServiceVO;
import io.choerodon.devops.api.vo.iam.DevopsEnvMessageVO;
import io.choerodon.devops.infra.dto.DevopsEnvAppServiceDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsEnvAppServiceMapper extends BaseMapper<DevopsEnvAppServiceDTO> {
    /**
     * 通过环境Id查询所有应用Id
     *
     * @param envId 环境Id
     * @return List 应用Id
     */
    List<Long> queryAppByEnvId(@Param("envId") Long envId);

    /**
     * 通过环境Id查询所有应用Id
     *
     * @param envId        环境Id
     * @param appServiceId 应用Id
     * @return List 环境资源信息
     */
    List<DevopsEnvMessageVO> listResourceByEnvAndApp(@Param("envId") Long envId, @Param("appServiceId") Long appServiceId);

    /**
     * 查询项目下可用的且没有与该环境关联的应用
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 应用列表
     */
    List<BaseApplicationServiceVO> listNonRelatedApplications(@Param("projectId") Long projectId,
                                                              @Param("envId") Long envId);

    /**
     * 查询应用服务部署的实例个数
     *
     * @param appServiceId 应用服务id， 必填
     * @param envId        环境ID， 选填，传值时查的是指定环境下的应用服务部署的实例个数
     * @param projectId    项目id，选填，传值时查的是指定项目下所有环境的下应用服务部署的实例个数
     * @return 个数
     */
    int countInstances(@Nonnull @Param("appServiceId") Long appServiceId,
                       @Nullable @Param("envId") Long envId,
                       @Nullable @Param("projectId") Long projectId);

    /**
     * 查询应用服务关联的网络个数
     *
     * @param appServiceId 应用服务id， 必填
     * @param envId        环境ID， 选填，传值时查的是指定环境下的应用服务关联的网络个数
     * @param projectId    项目id，选填，传值时查的是指定项目下所有环境的下应用服务关联的网络个数
     * @return 个数
     */
    int countRelatedService(@Nonnull @Param("appServiceId") Long appServiceId,
                            @Nullable @Param("envId") Long envId,
                            @Nullable @Param("projectId") Long projectId);

    /**
     * 如上
     */
    int countRelatedSecret(@Nonnull @Param("appServiceId") Long appServiceId,
                           @Nullable @Param("envId") Long envId,
                           @Nullable @Param("projectId") Long projectId);

    /**
     * 如上
     */
    int countRelatedConfigMap(@Nonnull @Param("appServiceId") Long appServiceId,
                              @Nullable @Param("envId") Long envId,
                              @Nullable @Param("projectId") Long projectId);

    /**
     * 删除应用服务和在此项目下的环境的关联关系
     *
     * @param appServiceId 应用服务id
     * @param projectId    项目id
     */
    void deleteRelevanceInProject(@Param("appServiceId") Long appServiceId,
                                  @Param("projectId") Long projectId);
}
