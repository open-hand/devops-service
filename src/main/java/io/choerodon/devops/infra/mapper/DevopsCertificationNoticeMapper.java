package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationNoticeDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCertificationNoticeMapper extends BaseMapper<CertificationNoticeDTO> {
    void deleteByCertificationId(@Param("certificationId") Long certificationId);

    List<CertificationNoticeDTO> listByCertificationIds(@Param("certificationIds") List<Long> certificationIds);
}
