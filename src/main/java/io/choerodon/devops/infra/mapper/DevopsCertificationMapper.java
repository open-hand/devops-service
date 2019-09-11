package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:57
 * Description:
 */

public interface DevopsCertificationMapper extends Mapper<CertificationDTO> {
    List<CertificationDTO> listCertificationByOptions(@Param("projectId") Long projectId,
                                                      @Param("envId") Long envId,
                                                      @Param("searchParam") Map<String, Object> searchParam,
                                                      @Param("params") List<String> params);

    List<CertificationDTO> queryActiveByDomain(@Param("projectId") Long projectId, @Param("clusterId") Long clusterId, @Param("domain") String domain);

    void updateSkipCheckPro(@Param("certId") Long certId, @Param("skipCheckPro") Boolean skipCheckPro);

    List<CertificationDTO> listByProjectId(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);

    List<CertificationDTO> listAllOrgCertification();

    void updateStatus(@Param("certId") Long certId, @Param("status") String status);
}
