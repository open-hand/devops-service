package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.AppServiceVersionReadmeDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * Creator: Runge
 * Date: 2018/6/19
 * Time: 11:17
 * Description:
 */
public interface AppServiceVersionReadmeMapper extends Mapper<AppServiceVersionReadmeDTO> {
    void deleteByIds(@Param("readmeIds") Set<Long> readmeIds);
}
