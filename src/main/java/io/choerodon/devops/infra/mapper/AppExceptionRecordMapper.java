package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppExceptionRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * chart应用异常信息记录表(AppExceptionRecord)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-05-10 11:17:14
 */
public interface AppExceptionRecordMapper extends BaseMapper<AppExceptionRecordDTO> {

    AppExceptionRecordDTO queryLatestExceptionRecord(@Param("appId") Long appId,
                                                     @Param("resourceType") String resourceType,
                                                     @Param("resourceName") String resourceName);

    List<AppExceptionRecordDTO> listByAppIdAndDate(@Param("appId") Long appId,
                                                   @Param("startTime") Date startTime,
                                                   @Param("endTime") Date endTime);
}

