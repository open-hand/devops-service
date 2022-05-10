package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.AppExceptionRecordService;
import io.choerodon.devops.infra.mapper.AppExceptionRecordMapper;

/**
 * chart应用异常信息记录表(AppExceptionRecord)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-05-10 11:17:14
 */
@Service
public class AppExceptionRecordServiceImpl implements AppExceptionRecordService {
    @Autowired
    private AppExceptionRecordMapper appExceptionRecordMapper;


}

