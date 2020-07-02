package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiCdStageRecordService;
import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:07
 */
@Service
public class CiCdStageRecordServiceImpl implements CiCdStageRecordService {

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
}
