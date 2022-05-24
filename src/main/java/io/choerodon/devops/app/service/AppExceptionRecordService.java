package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.AppExceptionRecordDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;

/**
 * chart应用异常信息记录表(AppExceptionRecord)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-05-10 11:17:14
 */
public interface AppExceptionRecordService {

    void createOrUpdateExceptionRecord(String resourceType, String resource, AppServiceInstanceDTO appServiceInstanceDTO);

    /**
     * 查询应用的异常记录，按日期筛选
     *
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 异常记录列表
     */
    List<AppExceptionRecordDTO> listByAppIdAndDate(Long appId, Date startTime, Date endTime);
}

