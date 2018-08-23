package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

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
    List<CertificationDO> selectCertification(@Param("searchParam") Map<String, Object> searchParam,
                                              @Param("param") String param);

    List<CertificationDO> getActiveByDomain(@Param("domain") String domain);
}
