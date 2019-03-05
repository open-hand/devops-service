package io.choerodon.devops.infra.mapper;

        import io.choerodon.devops.infra.dataobject.DevopsAutoDeployDO;
        import io.choerodon.mybatis.common.BaseMapper;
        import org.apache.ibatis.annotations.Param;

        import java.util.List;
        import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:27 2019/2/25
 * Description:
 */
public interface DevopsAutoDeployMapper extends BaseMapper<DevopsAutoDeployDO> {
    List<DevopsAutoDeployDO> list(@Param("projectId") Long projectId,
                                  @Param("appId") Long appId,
                                  @Param("envId") Long envId,
                                  @Param("searchParam") Map<String, Object> searchParam,
                                  @Param("param") String param);

    DevopsAutoDeployDO queryById(@Param("autoDeployId") Long autoDeployId);

    List<DevopsAutoDeployDO> queryByVersion(@Param("appId") Long versionId,
                                            @Param("branch") String branch);

    List<DevopsAutoDeployDO> checkTaskName(@Param("id") Long id,
                                           @Param("projectId") Long projectId,
                                           @Param("taskName") String taskName);

    void updateInstanceId(@Param("instanceId") Long instanceId);
}
