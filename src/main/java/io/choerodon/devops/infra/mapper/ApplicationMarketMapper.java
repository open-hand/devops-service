package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsAppShareDO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationMarketMapper extends Mapper<DevopsAppShareDO> {
    List<DevopsAppShareDO> listMarketApplicationInProject(@Param("projectId") Long projectId,
                                                          @Param("searchParam") Map<String, Object> searchParam,
                                                          @Param("param") String param);

    List<DevopsAppShareDO> listMarketAppsBySite(@Param("searchParam") Map<String, Object> searchParam, @Param("param") String param);

    List<DevopsAppShareDO> listMarketApplication(@Param("projectIds") List projectIds,
                                                 @Param("searchParam") Map<String, Object> searchParam,
                                                 @Param("param") String param);

    DevopsAppShareDO getMarketApplication(@Param("projectId") Long projectId,
                                          @Param("appMarketId") Long appMarketId,
                                          @Param("projectIds") List<Long> projectIds);

    int selectCountByAppId(@Param("appId") Long appId);

    int checkProject(@Param("projectId") Long projectId, @Param("appMarketId") Long appMarketId);

    int checkDeployed(@Param("projectId") Long projectId,
                      @Param("appMarketId") Long appMarketId,
                      @Param("versionId") Long versionId,
                      @Param("projectIds") List<Long> projectIds);

    void changeApplicationVersions(@Param("appMarketId") Long appMarketId,
                                   @Param("versionId") Long versionId,
                                   @Param("isPublish") Boolean isPublish);

    Long getMarketIdByAppId(@Param("appId") Long appId);


    List<DevopsAppMarketVersionDO> listAppVersions(@Param("projectIds") List<Long> projectIds,
                                                   @Param("appMarketId") Long appMarketId,
                                                   @Param("isPublish") Boolean isPublish,
                                                   @Param("searchParam") Map<String, Object> searchParam,
                                                   @Param("param") String param);

    Boolean checkVersion(@Param("appMarketId") Long appMarketId, @Param("versionId") Long versionId);
}
