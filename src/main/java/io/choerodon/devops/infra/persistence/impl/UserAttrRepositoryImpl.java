package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.dataobject.UserAttrDTO;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
public class UserAttrRepositoryImpl implements UserAttrRepository {

    private UserAttrMapper userAttrMapper;

    public UserAttrRepositoryImpl(UserAttrMapper userAttrMapper) {
        this.userAttrMapper = userAttrMapper;
    }

    @Override
    public int insert(UserAttrE userAttrE) {
        return userAttrMapper.insert(ConvertHelper.convert(userAttrE, UserAttrDTO.class));
    }

    @Override
    public UserAttrE queryById(Long id) {
        return ConvertHelper.convert(userAttrMapper.selectByPrimaryKey(id), UserAttrE.class);
    }

    @Override
    public Long queryUserIdByGitlabUserId(Long gitLabUserId) {
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
    public List<UserAttrE> listByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(userAttrMapper.listByUserIds(userIds), UserAttrE.class);
    }

    public UserAttrE queryByGitlabUserId(Long gitlabUserId) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        userAttrDO.setGitlabUserId(gitlabUserId);
        return ConvertHelper.convert(userAttrMapper.selectOne(userAttrDO), UserAttrE.class);
    }

    @Override
    public void update(UserAttrE userAttrE) {
        UserAttrDTO userAttrDO = userAttrMapper.selectByPrimaryKey(userAttrE.getIamUserId());
        userAttrDO.setGitlabToken(userAttrE.getGitlabToken());
        userAttrDO.setGitlabUserName(userAttrE.getGitlabUserName());
        userAttrMapper.updateByPrimaryKey(userAttrDO);
    }

    @Override
    public List<UserAttrE> list() {
        return ConvertHelper.convertList(userAttrMapper.selectAll(), UserAttrE.class);
    }

    @Override
    public UserAttrE queryByGitlabUserName(String gitlabUserName) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        userAttrDO.setGitlabUserName(gitlabUserName);
        return ConvertHelper.convert(userAttrMapper.selectOne(userAttrDO), UserAttrE.class);
    }

}
