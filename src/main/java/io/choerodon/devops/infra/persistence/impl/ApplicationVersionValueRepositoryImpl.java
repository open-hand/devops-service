package io.choerodon.devops.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionValueE;
import io.choerodon.devops.domain.application.repository.ApplicationVersionValueRepository;
import io.choerodon.devops.infra.dto.ApplicationVersionValueDO;
import io.choerodon.devops.infra.mapper.ApplicationVersionValueMapper;

@Service
public class ApplicationVersionValueRepositoryImpl implements ApplicationVersionValueRepository {

    private ApplicationVersionValueMapper applcationVersionValueMapper;

    public ApplicationVersionValueRepositoryImpl(ApplicationVersionValueMapper applcationVersionValueMapper) {
        this.applcationVersionValueMapper = applcationVersionValueMapper;
    }

    @Override
    public ApplicationVersionValueE create(ApplicationVersionValueE applicationVersionValueE) {
        ApplicationVersionValueDO applicationVersionValueDO = ConvertHelper
                .convert(applicationVersionValueE, ApplicationVersionValueDO.class);
        if (applcationVersionValueMapper.insert(applicationVersionValueDO) != 1) {
            throw new CommonException("error.version.value.insert");
        }
        return ConvertHelper.convert(applicationVersionValueDO, ApplicationVersionValueE.class);
    }

    @Override
    public ApplicationVersionValueE query(Long appVersionValueId) {
        return ConvertHelper.convert(applcationVersionValueMapper.selectByPrimaryKey(appVersionValueId), ApplicationVersionValueE.class);
    }
}
