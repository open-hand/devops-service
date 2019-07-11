package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    @Autowired
    private UserAttrRepository userAttrRepository;

    @Override
    public UserAttrVO queryByUserId(Long userId) {
        return ConvertHelper.convert(userAttrRepository.queryById(userId), UserAttrVO.class);
    }
}
