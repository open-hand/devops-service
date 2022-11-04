package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.appversion.AppServiceHelmVersionVO;
import io.choerodon.devops.infra.dto.AppServiceHelmVersionDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 应用版本表(AppServiceHelmVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:41
 */
public interface AppServiceHelmVersionMapper extends BaseMapper<AppServiceHelmVersionDTO> {

    List<AppServiceHelmVersionVO> listByAppVersionIds(@Param("versionIds") Set<Long> versionIds);

    void batchInsert(@Param("appServiceHelmVersionDTOToInsert") List<AppServiceHelmVersionDTO> appServiceHelmVersionDTOToInsert);

    Integer count();
}

