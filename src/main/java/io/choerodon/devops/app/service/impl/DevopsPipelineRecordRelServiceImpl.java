package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsPipelineRecordRelService;
import io.choerodon.devops.infra.mapper.DevopsPipelineRecordRelMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 20:52
 */
@Service
public class DevopsPipelineRecordRelServiceImpl implements DevopsPipelineRecordRelService {
    @Autowired
    private DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;


}
