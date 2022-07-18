package io.choerodon.devops.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.devops.infra.dto.AppServiceHelmRelDTO;

/**
 * 应用服务和helm配置的关联关系表(AppServiceHelmRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-15 10:55:52
 */
public interface AppServiceHelmRelMapper extends BaseMapper<AppServiceHelmRelDTO> {
}
