package io.choerodon.devops.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.CiScheduleVariableService;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.dto.CiScheduleVariableDTO;
import io.choerodon.devops.infra.mapper.CiScheduleVariableMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * devops_ci_schedule_variable(CiScheduleVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:52
 */
@Service
public class CiScheduleVariableServiceImpl implements CiScheduleVariableService {
    @Autowired
    private CiScheduleVariableMapper ciScheduleVariableMapper;


}

