package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.ApplicationShareVersionDTO;
import io.choerodon.devops.infra.dto.ApplicationShareDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationShareMapper extends Mapper<ApplicationShareDTO> {
    List<ApplicationShareDTO> listMarketApplicationInProject(@Param("projectId") Long projectId,
                                                             @Param("searchParam") Map<String, Object> searchParam,
                                                             @Param("param") String param);

    List<ApplicationShareDTO> listMarketAppsBySite(@Param("isSite") Boolean isSite,
                                                   @Param("isFree") Boolean isFree,
                                                   @Param("searchParam") Map<String, Object> searchParam,
                                                   @Param("param") String param);

    List<ApplicationShareDTO> listMarketApplication(@Param("projectIds") List projectIds,
                                                    @Param("searchParam") Map<String, Object> searchParam,
                                                    @Param("param") String param);

    ApplicationShareDTO queryByShareId(@Param("projectId") Long projectId,
                                       @Param("appMarketId") Long appMarketId,
                                       @Param("projectIds") List<Long> projectIds);

    int countByAppId(@Param("appId") Long appId);

    int checkByProjectId(@Param("projectId") Long projectId, @Param("appMarketId") Long appMarketId);

    int checkByDeployed(@Param("projectId") Long projectId,
                        @Param("appMarketId") Long appMarketId,
                        @Param("versionId") Long versionId,
                        @Param("projectIds") List<Long> projectIds);

    void changeApplicationVersions(@Param("appMarketId") Long appMarketId,
                                   @Param("versionId") Long versionId,
                                   @Param("isPublish") Boolean isPublish);

    Long baseQueryShareIdByAppId(@Param("appId") Long appId);


    List<ApplicationShareVersionDTO> listAppVersions(@Param("projectIds") List<Long> projectIds,
                                                     @Param("appMarketId") Long appMarketId,
                                                     @Param("isPublish") Boolean isPublish,
                                                     @Param("searchParam") Map<String, Object> searchParam,
                                                     @Param("param") String param);

    Boolean checkByShareIdAndVersion(@Param("appMarketId") Long appMarketId, @Param("versionId") Long versionId);

    List<ApplicationShareDTO> queryByShareIds(@Param("searchParam") Map<String, Object> searchParam,
                                              @Param("param") String param,
                                              @Param("shareIds") List<Long> shareIds);

    void updatePublishLevel();
}
