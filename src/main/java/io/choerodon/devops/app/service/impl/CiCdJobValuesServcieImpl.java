package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CiCdJobValuesServcie;
import io.choerodon.devops.infra.dto.CiCdJobValuesDTO;
import io.choerodon.devops.infra.mapper.CiCdJobValuesMapper;

@Service
public class CiCdJobValuesServcieImpl implements CiCdJobValuesServcie{

    private static final String CREATE_CI_CONTENT_FAILED = "create.ci.content.failed";

    @Autowired
    private CiCdJobValuesMapper ciCdJobValuesMapper;

    @Override
    public void create(CiCdJobValuesDTO ciCdJobValuesDTO) {
        if (ciCdJobValuesMapper.insertSelective(ciCdJobValuesDTO) != 1) {
            throw new CommonException(CREATE_CI_CONTENT_FAILED);
        }
    }
}
