package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import io.choerodon.asgard.saga.feign.SagaConsumerClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private SagaConsumerClient sagaConsumerClient;
    @Autowired
    private UserAttrMapper userAttrMapper;

    @Override
    public UserAttrVO queryByUserId(Long userId) {
        return ConvertHelper.convert(userAttrRepository.baseQueryById(userId), UserAttrVO.class);
    }


    @Override
    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }


    @Override
    public Long getUserIdByGitlabUserId(Long gitLabUserId) {
        try {
            return userAttrRepository.baseQueryUserIdByGitlabUserId(gitLabUserId);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public int baseInsert(UserAttrDTO userAttrDTO) {
        return userAttrMapper.insert(userAttrDTO);
    }

    @Override
    public UserAttrDTO baseQueryById(Long id) {
        return userAttrMapper.selectByPrimaryKey(id);
    }

    @Override
    public Long baseQueryUserIdByGitlabUserId(Long gitLabUserId) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        userAttrDO.setGitlabUserId(gitLabUserId);
        if (gitLabUserId == null) {
            return null;
        }
        userAttrDO = userAttrMapper.selectOne(userAttrDO);
        if (userAttrDO == null) {
            return null;
        } else {
            return userAttrDO.getIamUserId();
        }
    }

    @Override
    public List<UserAttrDTO> baseListByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userAttrMapper.listByUserIds(userIds);
    }

    public UserAttrDTO baseQueryByGitlabUserId(Long gitlabUserId) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        userAttrDO.setGitlabUserId(gitlabUserId);
        return userAttrMapper.selectOne(userAttrDO);
    }

    @Override
    public void baseUpdate(UserAttrDTO userAttrDTO) {
        UserAttrDTO userAttrDO = userAttrMapper.selectByPrimaryKey(userAttrDTO.getIamUserId());
        userAttrDO.setGitlabToken(userAttrDTO.getGitlabToken());
        userAttrDO.setGitlabUserName(userAttrDTO.getGitlabUserName());
        userAttrMapper.updateByPrimaryKey(userAttrDO);
    }

    @Override
    public List<UserAttrDTO> baseList() {
        return userAttrMapper.selectAll();
    }

    @Override
    public UserAttrDTO baseQueryByGitlabUserName(String gitlabUserName) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        userAttrDO.setGitlabUserName(gitlabUserName);
        return userAttrMapper.selectOne(userAttrDO);
    }
}
