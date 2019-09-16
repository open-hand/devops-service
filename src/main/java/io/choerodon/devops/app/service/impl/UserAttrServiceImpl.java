package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    @Autowired
    private UserAttrMapper userAttrMapper;

    @Override
    public UserAttrVO queryByUserId(Long userId) {
        return ConvertUtils.convertObject(baseQueryById(userId), UserAttrVO.class);
    }

    @Override
    public Long queryUserIdByGitlabUserId(Long gitLabUserId) {
        try {
            return baseQueryUserIdByGitlabUserId(gitLabUserId);
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
        if (gitLabUserId == null) {
            return null;
        }

        UserAttrDTO userAttrDTO = new UserAttrDTO();
        userAttrDTO.setGitlabUserId(gitLabUserId);
        userAttrDTO = userAttrMapper.selectOne(userAttrDTO);

        return userAttrDTO == null ? null : userAttrDTO.getIamUserId();
    }

    @Override
    public List<UserAttrDTO> baseListByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userAttrMapper.listByUserIds(userIds);
    }

    @Override
    public UserAttrDTO baseQueryByGitlabUserId(Long gitlabUserId) {
        UserAttrDTO userAttrDTO = new UserAttrDTO();
        userAttrDTO.setGitlabUserId(gitlabUserId);
        return userAttrMapper.selectOne(userAttrDTO);
    }

    @Override
    public void baseUpdate(UserAttrDTO userAttrDTO) {
        UserAttrDTO newUserAttrDTO = userAttrMapper.selectByPrimaryKey(userAttrDTO.getIamUserId());
        newUserAttrDTO.setGitlabToken(userAttrDTO.getGitlabToken());
        newUserAttrDTO.setGitlabUserName(userAttrDTO.getGitlabUserName());
        userAttrMapper.updateByPrimaryKey(newUserAttrDTO);
    }

    @Override
    public UserAttrDTO baseQueryByGitlabUserName(String gitlabUserName) {
        UserAttrDTO userAttrDTO = new UserAttrDTO();
        userAttrDTO.setGitlabUserName(gitlabUserName);
        return userAttrMapper.selectOne(userAttrDTO);
    }

}
