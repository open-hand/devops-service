package io.choerodon.devops.api.vo;

import static io.choerodon.devops.app.service.impl.DevopsHostServiceImpl.PERMISSION_LABEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.infra.dto.iam.RoleDTO;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageInfoUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public class DevopsHostUserPermissionVO extends DevopsUserPermissionVO {
    @ApiModelProperty("主机权限标签")
    private String permissionLabel;

    public static Page<DevopsHostUserPermissionVO> combine(List<DevopsHostUserPermissionVO> allProjectMemberPermissions, List<DevopsHostUserPermissionVO> allProjectOwnerPermissions, List<DevopsUserPermissionVO> allProjectMembers, PageRequest pageable, Long creatorId, Map<String, Object> searchParamMap) {
        List<DevopsHostUserPermissionVO> userPermissionVOS = new ArrayList<>(allProjectOwnerPermissions);
        userPermissionVOS.addAll(allProjectMemberPermissions);
        if (userPermissionVOS.isEmpty()) {
            return ConvertUtils.convertPage(new Page<>(), DevopsHostUserPermissionVO.class);
        } else {
            List<DevopsHostUserPermissionVO> resultPermissionVOs = new ArrayList<>();
            Map<Long, List<DevopsHostUserPermissionVO>> hostPermissionMaps = userPermissionVOS.stream().collect(Collectors.groupingBy(DevopsHostUserPermissionVO::getIamUserId));
            Map<Long, DevopsUserPermissionVO> projectMemberPermissionMaps = allProjectMembers.stream().collect(Collectors.toMap(DevopsUserPermissionVO::getIamUserId, Function.identity()));
            for (Map.Entry<Long, List<DevopsHostUserPermissionVO>> entry : hostPermissionMaps.entrySet()) {
                DevopsHostUserPermissionVO hostUserPermissionVO = entry.getValue().get(0);
                List<RoleDTO> roleDTOS = hostUserPermissionVO.getRoles() == null ? new ArrayList<>() : hostUserPermissionVO.getRoles();
                if (entry.getValue().size() > 1) {
                    entry.getValue().forEach(v -> roleDTOS.addAll(v.getRoles()));
                }
                // 这步操作是为了把拥有项目成员角色的其它角色也提取出来
                if (projectMemberPermissionMaps.get(hostUserPermissionVO.getIamUserId()) != null) {
                    roleDTOS.addAll(projectMemberPermissionMaps.get(hostUserPermissionVO.getIamUserId()).getRoles());
                }
                hostUserPermissionVO.setRoles(roleDTOS);
                if (hostUserPermissionVO.getIamUserId().equals(creatorId)) {
                    hostUserPermissionVO.setCreator(true);
                }
                resultPermissionVOs.add(hostUserPermissionVO);
            }
            if (!StringUtils.isEmpty(searchParamMap.get(PERMISSION_LABEL))) {
                resultPermissionVOs = resultPermissionVOs.stream().filter(permission -> permission.getPermissionLabel().equals(searchParamMap.get(PERMISSION_LABEL))).collect(Collectors.toList());
            }
            resultPermissionVOs = PageRequestUtil.sortHostUserPermission(resultPermissionVOs, pageable.getSort());
            return PageInfoUtil.createPageFromList(new ArrayList<>(resultPermissionVOs), pageable);
        }
    }

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(String permissionLabel) {
        this.permissionLabel = permissionLabel;
    }
}
