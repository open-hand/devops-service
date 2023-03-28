package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CertificationFileDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:57
 * Description:
 */

public interface DevopsCertificationFileMapper extends BaseMapper<CertificationFileDTO> {
    CertificationFileDTO queryByCertificationId(@Param("certificationId") Long certificationId);

    List<CertificationFileDTO> listByCertificationIds(@Param("certificationIds") List<Long> certificationIds);
}

