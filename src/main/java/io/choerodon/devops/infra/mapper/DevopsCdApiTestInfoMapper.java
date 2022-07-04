package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsCdApiTestInfoDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * devops_cd_api_test_info(DevopsCdApiTestInfo)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-20 09:57:26
 */
public interface DevopsCdApiTestInfoMapper extends BaseMapper<DevopsCdApiTestInfoDTO> {

    Boolean doesApiTestSuiteRelatedWithPipeline(Long suiteId);
}

