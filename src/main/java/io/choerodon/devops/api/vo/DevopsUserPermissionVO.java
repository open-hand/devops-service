package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.RoleDTO;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageInfoUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 */
public class DevopsUserPermissionVO extends DevopsUserVO {
    private List<RoleDTO> roles;
    private Date creationDate;
    private Boolean gitlabProjectOwner;
    private Boolean isCreator;

    public static DevopsUserPermissionVO iamUserTOUserPermissionVO(IamUserDTO iamUserDTO, Boolean isGitlabProjectOwner) {
        DevopsUserPermissionVO devopsUserPermissionVO = new DevopsUserPermissionVO();
        devopsUserPermissionVO.setIamUserId(iamUserDTO.getId());
        if (iamUserDTO.getLdap()) {
            devopsUserPermissionVO.setLoginName(iamUserDTO.getLoginName());
        } else {
            devopsUserPermissionVO.setLoginName(iamUserDTO.getEmail());
        }
        devopsUserPermissionVO.setImageUrl(iamUserDTO.getImageUrl());
        devopsUserPermissionVO.setRealName(iamUserDTO.getRealName());
        devopsUserPermissionVO.setRoles(iamUserDTO.getRoles());
        devopsUserPermissionVO.setCreationDate(iamUserDTO.getCreationDate());
        devopsUserPermissionVO.setGitlabProjectOwner(isGitlabProjectOwner);
        return devopsUserPermissionVO;
    }

    public static Page<DevopsUserPermissionVO> combineOwnerAndMember(List<DevopsUserPermissionVO> allProjectMembers, List<DevopsUserPermissionVO> allProjectOwners, PageRequest pageable) {
        List<DevopsUserPermissionVO> userPermissionVOS = new ArrayList<>(allProjectOwners);
        userPermissionVOS.addAll(allProjectMembers);
        if (userPermissionVOS.isEmpty()) {
            return ConvertUtils.convertPage(new Page<>(), DevopsUserPermissionVO.class);
        } else {
            List<DevopsUserPermissionVO> resultPermissionVOs = new ArrayList<>();
            Map<Long, List<DevopsUserPermissionVO>> maps = userPermissionVOS.stream().collect(Collectors.groupingBy(DevopsUserPermissionVO::getIamUserId));
            for (Map.Entry<Long, List<DevopsUserPermissionVO>> entry : maps.entrySet()) {
                DevopsUserPermissionVO userPermissionVO = entry.getValue().get(0);
                if (entry.getValue().size() > 1) {
                    List<RoleDTO> roleDTOS = new ArrayList<>();
                    entry.getValue().forEach(v -> roleDTOS.addAll(v.getRoles()));
                    userPermissionVO.setRoles(roleDTOS);
                }
                resultPermissionVOs.add(userPermissionVO);
            }
            resultPermissionVOs = PageRequestUtil.sortUserPermission(resultPermissionVOs, pageable.getSort());
            return PageInfoUtil.createPageFromList(new ArrayList<>(resultPermissionVOs), pageable);
        }
    }

    public DevopsUserPermissionVO() {
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName) {
        super(iamUserId, loginName, realName);
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName, Date creationDate) {
        super(iamUserId, loginName, realName);
        this.creationDate = creationDate;
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName, String imageUrl) {
        super(iamUserId, loginName, realName, imageUrl);
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    public Boolean getGitlabProjectOwner() {
        return gitlabProjectOwner;
    }

    public void setGitlabProjectOwner(Boolean gitlabProjectOwner) {
        this.gitlabProjectOwner = gitlabProjectOwner;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int hashCode() {
        String in = super.getIamUserId() + super.getLoginName() + super.getRealName();
        return in.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        DevopsUserPermissionVO s = (DevopsUserPermissionVO) obj;
        return super.getIamUserId().equals(s.getIamUserId());
    }

    public Boolean getCreator() {
        return isCreator;
    }

    public void setCreator(Boolean creator) {
        isCreator = creator;
    }
}
