package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.CertificationDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:57
 * Description:
 */

public interface DevopsCertificationMapper extends Mapper<CertificationDO> {
    List<CertificationDO> selectCertification(@Param("projectId") Long projectId,
                                              @Param("organizationId") Long organizationId,
                                              @Param("envId") Long envId,
                                              @Param("searchParam") Map<String, Object> searchParam,
                                              @Param("param") String param);

    List<CertificationDO> getActiveByDomain(@Param("projectId") Long projectId, @Param("clusterId") Long clusterId,@Param("domain") String domain);

    void updateSkipCheckPro(@Param("certId") Long clusterId, @Param("skipCheckPro") Boolean skipCheckPro);

    List<CertificationDO> listByProjectId(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);
}
