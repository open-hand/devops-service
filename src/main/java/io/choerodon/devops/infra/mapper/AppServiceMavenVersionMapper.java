package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.appversion.AppServiceMavenVersionVO;
import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.devops.infra.dto.AppServiceMavenVersionDTO;

/**
 * 应用版本表(AppServiceMavenVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:43
 */
public interface AppServiceMavenVersionMapper extends BaseMapper<AppServiceMavenVersionDTO> {
    List<AppServiceMavenVersionVO> listByAppVersionIds(@Param("versionIds") Set<Long> versionIds);
}

