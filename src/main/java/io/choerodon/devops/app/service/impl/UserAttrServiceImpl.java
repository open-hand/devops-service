package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_IAM_USER_SYNC_TO_GITLAB;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.IamAdminIdHolder;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class UserAttrServiceImpl implements UserAttrService {

    private static final String DEVOPS_INSERT_USER = "devops.insert.user";
    private static final String DEVOPS_GET_IAM_ADMIN = "devops.get.iam.admin";

    @Autowired
    private UserAttrMapper userAttrMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private AppServiceService appServiceService;

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
        CommonExAssertUtil.assertTrue(userAttrDTO != null && userAttrDTO.getGitlabUserId() != null, DEVOPS_IAM_USER_SYNC_TO_GITLAB, iamUserId);
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
        MapperUtil.resultJudgedInsertSelective(userAttrMapper, userAttrDTO, DEVOPS_INSERT_USER);
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
    public List<UserAttrDTO> baseListByGitlabUserIds(List<Long> gitlabUserIds) {
        if (gitlabUserIds == null || gitlabUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userAttrMapper.listByGitlabUserIds(new ArrayList<>(gitlabUserIds));
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
    public Long getIamUserIdByGitlabUserName(String username) {
        if ("admin1".equals(username) || "root".equals(username)) {
            return IamAdminIdHolder.getAdminId();
        }
        UserAttrDTO userAttrE = baseQueryByGitlabUserName(username);
        return userAttrE != null ? userAttrE.getIamUserId() : 0L;
    }

    @Override
    public void updateAdmin(Long iamUserId, Boolean isGitlabAdmin) {
        userAttrMapper.updateIsGitlabAdmin(Objects.requireNonNull(iamUserId), Objects.requireNonNull(isGitlabAdmin));
    }

    @Override
    public void updateAdmins(List<Long> iamUserIds, Boolean isGitlabAdmin) {
        if (ObjectUtils.isEmpty(iamUserIds)) {
            return;
        }
        userAttrMapper.updateAreAdmin(iamUserIds, isGitlabAdmin);
    }

    @Override
    public Page<IamUserDTO> queryByAppServiceId(Long projectId, Long appServiceId, PageRequest pageRequest, String params) {
        List<Long> selectedIamUserIds = new ArrayList<>();
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);

        String realName = null;
        if (!StringUtils.isEmpty(params)) {
            Map maps = JSONObject.parseObject(params, Map.class);
            selectedIamUserIds = KeyDecryptHelper.decryptIdList((JSONArray) maps.get("ids"));
            realName = (String) maps.get("userName");
        }

        if (appServiceDTO.getExternalConfigId() == null) {
            List<MemberDTO> memberDTOS = gitlabServiceClientOperator.listMemberByProject(appServiceDTO.getGitlabProjectId(), realName);
            if (CollectionUtils.isEmpty(memberDTOS)) {
                return new Page<>();
            }
            Set<Long> guids = memberDTOS.stream().filter(v -> v.getAccessLevel() >= AccessLevel.DEVELOPER.value).map(m -> m.getId().longValue()).collect(Collectors.toSet());

            List<UserAttrVO> userAttrVOS = listUsersByGitlabUserIds(guids);

            List<Long> uids = userAttrVOS.stream().filter(u -> u.getIamUserId() != null).map(UserAttrVO::getIamUserId).collect(Collectors.toList());

            // allUserIds构成
            // 1. 用户选中的
            // 2. 项目下搜索到的
            List<Long> allUserIds = new ArrayList<>(new HashSet<>(selectedIamUserIds));

            if (!CollectionUtils.isEmpty(uids)) {
                uids.forEach(uid -> {
                    if (!allUserIds.contains(uid)) {
                        allUserIds.add(uid);
                    }
                });
            }
            if (CollectionUtils.isEmpty(allUserIds)) {
                return new Page<>();
            }
            List<IamUserDTO> userDTOS = allUserIds.stream().map(v -> {
                IamUserDTO iamUserDTO = new IamUserDTO();
                iamUserDTO.setId(v);
                return iamUserDTO;
            }).collect(Collectors.toList());

            Page<IamUserDTO> page = PageInfoUtil.doPageFromList(userDTOS, pageRequest);

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
        } else {
            return baseServiceClientOperator.pagingQueryUsersWithRolesOnProjectLevel(projectId, pageRequest.getPage(), pageRequest.getSize(), realName);
        }
    }

    @Override
    public int allUserCount() {
        return userAttrMapper.selectCount(new UserAttrDTO());
    }

    @Override
    public Set<Long> allUserIds() {
        return userAttrMapper.selectAllUserIds();
    }

    @Override
    public UserAttrDTO queryGitlabAdminByIamId() {
        Optional<IamUserDTO> optional = baseServiceClientOperator.queryRoot().stream().filter(t -> t.getLoginName().equals("admin")).findFirst();
        if (optional.isPresent()) {
            UserAttrDTO attrDTO = new UserAttrDTO();
            attrDTO.setIamUserId(optional.get().getId());
            return userAttrMapper.selectOne(attrDTO);
        } else {
            throw new CommonException(DEVOPS_GET_IAM_ADMIN);
        }
    }

    @Override
    public List<UserAttrVO> listAllAdmin() {
        return userAttrMapper.listAllAdmin();
    }

    @Override
    public void updateGitlabAdminUserToNormalUser(List<Long> iamUserIds) {
        userAttrMapper.updateGitlabAdminUserToNormalUser(iamUserIds);
    }

    @Override
    public String queryOrCreateImpersonationToken(Long iamUserId) {
        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(iamUserId);

        if (org.apache.commons.lang3.StringUtils.isEmpty(userAttrDTO.getGitlabToken())) {
            String impersonationToken = gitlabServiceClientOperator.createImpersonationToken(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), null);
            userAttrDTO.setGitlabToken(impersonationToken);
            userAttrMapper.updateByPrimaryKeySelective(userAttrDTO);
            return impersonationToken;
        } else {
            return userAttrDTO.getGitlabToken();
        }
    }
}
