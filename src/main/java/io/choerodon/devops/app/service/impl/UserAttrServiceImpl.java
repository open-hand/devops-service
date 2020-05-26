package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    @Autowired
    private UserAttrMapper userAttrMapper;

    @Override
    public UserAttrVO queryByUserId(Long userId) {
        return ConvertUtils.convertObject(baseQueryById(userId), UserAttrVO.class);
    }

    @Override
    public List<UserAttrVO> listByUserIds(Set<Long> userIds) {
        return ConvertUtils.convertList(baseListByUserIds(new ArrayList<>(userIds)), UserAttrVO.class);
    }

    @Override
    public List<UserAttrVO> listUsersByGitlabUserIds(Set<Long> gitlabUserIds) {
        if (CollectionUtils.isEmpty(gitlabUserIds)) {
            return Collections.emptyList();
        }
        // 查出数据库有的
        List<UserAttrDTO> users = userAttrMapper.listByGitlabUserIds(new ArrayList<>(gitlabUserIds));
        List<UserAttrVO> result = new ArrayList<>();

        // 将数据库查出的转化类型，并将有的从输入的集合中去除
        users.forEach(user -> {
            gitlabUserIds.remove(user.getGitlabUserId());
            UserAttrVO userAttrVO = new UserAttrVO(user.getIamUserId(), user.getGitlabUserId());
            result.add(userAttrVO);
        });
        // 将数据库中没有的进行处理加入返回的集合
        result.addAll(gitlabUserIds.stream().map(gitlabUserId -> new UserAttrVO(null, gitlabUserId)).collect(Collectors.toList()));
        return result;
    }

    @Override
    public UserAttrDTO checkUserSync(UserAttrDTO userAttrDTO, Long iamUserId) {
        CommonExAssertUtil.assertTrue(userAttrDTO != null && userAttrDTO.getGitlabUserId() != null, "error.iam.user.sync.to.gitlab", iamUserId);
        return userAttrDTO;
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
    public void baseInsert(UserAttrDTO userAttrDTO) {
        MapperUtil.resultJudgedInsertSelective(userAttrMapper, userAttrDTO, "error.insert.user");
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

    @Override
    public void updateAdmin(Long iamUserId, Boolean isGitlabAdmin) {
        userAttrMapper.updateIsGitlabAdmin(Objects.requireNonNull(iamUserId), Objects.requireNonNull(isGitlabAdmin));
    }
}
