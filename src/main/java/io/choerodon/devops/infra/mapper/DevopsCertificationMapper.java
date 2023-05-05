package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:57
 * Description:
 */

public interface DevopsCertificationMapper extends BaseMapper<CertificationDTO> {
    List<CertificationDTO> listCertificationByOptions(@Param("projectId") Long projectId,
                                                      @Param("envId") Long envId,
                                                      @Param("searchParam") Map<String, Object> searchParam,
                                                      @Param("params") List<String> params);

    List<CertificationDTO> queryActiveByDomain(@Param("projectId") Long projectId, @Param("clusterId") Long clusterId, @Param("domain") String domain);

    void updateSkipCheckPro(@Param("certId") Long certId, @Param("skipCheckPro") Boolean skipCheckPro);

    List<CertificationDTO> listByProjectId(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);

    List<CertificationDTO> listAllOrgCertification();

    void updateStatus(@Param("certId") Long certId, @Param("status") String status);

    int updateStatusIfOperating(@Param("certId") Long certId, @Param("status") String status);

    CertificationDTO queryById(@Param("certId") Long certId);

    /**
     * 查询证书信息及其command字段
     *
     * @param certId 证书id
     * @return 证书信息
     */
    CertificationDTO queryDetailById(@Param("certId") Long certId);

    List<CertificationDTO> listAllOrgCertificationToMigrate();

    List<CertificationDTO> listClusterCertification(@Param("clusterId") Long clusterId);

    List<CertificationDTO> listByIds(@Param("ids") List<Long> ids);

    Boolean checkNameUnique(@Param("envId") Long envId, @Param("certName") String certName, @Param("certId") Long certId);

    List<CertificationDTO> listExpireCertificate();

    void updateAdvanceDaysToNull(@Param("id") Long id);

    int queryCountWithNullType();

    List<CertificationDTO> listWithNullType();

    List<CertificationDTO> queryActive(@Param("projectId") Long projectId, @Param("envId") Long envId);
}
