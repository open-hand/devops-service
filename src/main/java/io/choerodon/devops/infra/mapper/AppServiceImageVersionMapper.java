package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.appversion.AppServiceImageVersionVO;
import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.devops.infra.dto.AppServiceImageVersionDTO;

/**
 * 应用版本表(AppServiceImageVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:42
 */
public interface AppServiceImageVersionMapper extends BaseMapper<AppServiceImageVersionDTO> {
    List<AppServiceImageVersionVO> listByAppVersionIds(@Param("versionIds") Set<Long> versionIds);

    void batchInsert(@Param("appServiceImageVersionDTOS") List<AppServiceImageVersionDTO> appServiceImageVersionDTOS);

}

