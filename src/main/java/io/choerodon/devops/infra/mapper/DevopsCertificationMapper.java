package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.CertificationDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:57
 * Description:
 */

public interface DevopsCertificationMapper extends BaseMapper<CertificationDO> {
    List<CertificationDO> selectCertification(@Param("envId") Long envId);

    List<CertificationDO> getActiveByDomain(@Param("envId") Long envId, @Param("domain") String domain);
}
