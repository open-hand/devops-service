package io.choerodon.devops.app.service.impl;

import io.choerodon.asgard.saga.feign.SagaConsumerClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.UserAttrDTO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private SagaConsumerClient sagaConsumerClient;

    @Override
    public UserAttrDTO queryByUserId(Long userId) {
        return ConvertHelper.convert(userAttrRepository.queryById(userId), UserAttrDTO.class);
    }
}
