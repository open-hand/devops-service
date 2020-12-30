package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberQueryDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberViewDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.HrdsCodeRepoClientOperator;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    @Autowired
    private UserAttrMapper userAttrMapper;
    @Autowired
    private HrdsCodeRepoClientOperator hrdsCodeRepoClientOperator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

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

    @Override
    public Page<IamUserDTO> queryByAppServiceId(Long projectId, Long appServiceId, PageRequest pageRequest, String params) {
        List<Long> selectedIamUserIds = new ArrayList<>();
        String realName = null;
        if (!StringUtils.isEmpty(params)) {
            Map maps = JSONObject.parseObject(params, Map.class);
            selectedIamUserIds = KeyDecryptHelper.decryptIdList((JSONArray) maps.get("ids"));
            realName = (String) maps.get("userName");
        }
        RdmMemberQueryDTO rdmMemberQueryDTO = new RdmMemberQueryDTO();
        rdmMemberQueryDTO.setRepositoryIds(Collections.singleton(appServiceId));
        rdmMemberQueryDTO.setRealName(realName);
        List<RdmMemberViewDTO> rdmMemberViewDTOS = hrdsCodeRepoClientOperator.listMembers(null, projectId, rdmMemberQueryDTO);

        List<Long> finalSelectedIamUserIds = selectedIamUserIds;
        List<IamUserDTO> userDTOS = rdmMemberViewDTOS.stream().map(v -> {
            IamUserDTO iamUserDTO = new IamUserDTO();
            iamUserDTO.setId(v.getUserId());
            if (finalSelectedIamUserIds.contains(v.getUserId())) {
                iamUserDTO.setSelectFlag(1);
            }
            return iamUserDTO;
        }).sorted(Comparator.comparingInt(IamUserDTO::getSelectFlag).reversed()).collect(Collectors.toList());
        Page<IamUserDTO> page = PageInfoUtil.createPageFromList(userDTOS, pageRequest);

        List<Long> userIds = page.getContent().stream().map(IamUserDTO::getId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
        page.getContent().forEach(v -> {
            IamUserDTO iamUserDTO = userMap.get(v.getId());
            if (iamUserDTO != null) {
                v.setLoginName(iamUserDTO.getLoginName());
                v.setRealName(iamUserDTO.getRealName());
                v.setLdap(iamUserDTO.getLdap());
                v.setEmail(iamUserDTO.getEmail());
            }
        });


        return page;
    }

    @Override
    public long allUserCount() {
        return userAttrMapper.selectCount(new UserAttrDTO());
    }

    @Override
    public Set<Long> allUserIds() {
        return userAttrMapper.selectAllUserIds();
    }
}
