package io.choerodon.devops.api.vo;

import static io.choerodon.devops.app.service.impl.DevopsHostServiceImpl.PERMISSION_LABEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static Page<DevopsHostUserPermissionVO> combine(List<DevopsHostUserPermissionVO> allProjectMembers, List<DevopsHostUserPermissionVO> allProjectOwners, PageRequest pageable, Long creatorId, Map<String, Object> searchParamMap) {
        List<DevopsHostUserPermissionVO> userPermissionVOS = new ArrayList<>(allProjectOwners);
        userPermissionVOS.addAll(allProjectMembers);
        if (userPermissionVOS.isEmpty()) {
            return ConvertUtils.convertPage(new Page<>(), DevopsHostUserPermissionVO.class);
        } else {
            List<DevopsHostUserPermissionVO> resultPermissionVOs = new ArrayList<>();
            Map<Long, List<DevopsHostUserPermissionVO>> maps = userPermissionVOS.stream().collect(Collectors.groupingBy(DevopsHostUserPermissionVO::getIamUserId));
            for (Map.Entry<Long, List<DevopsHostUserPermissionVO>> entry : maps.entrySet()) {
                DevopsHostUserPermissionVO userPermissionVO = entry.getValue().get(0);
                if (entry.getValue().size() > 1) {
                    List<RoleDTO> roleDTOS = new ArrayList<>();
                    entry.getValue().forEach(v -> roleDTOS.addAll(v.getRoles()));
                    userPermissionVO.setRoles(roleDTOS);
                }
                if (userPermissionVO.getIamUserId().equals(creatorId)) {
                    userPermissionVO.setCreator(true);
                }
                resultPermissionVOs.add(userPermissionVO);
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
