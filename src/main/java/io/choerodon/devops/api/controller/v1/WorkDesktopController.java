package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.app.service.WorkDesktopService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 个人工作台
 *
 * @author lihao
 */
@RestController
@RequestMapping("/v1/desktop/{organization_id}")
public class WorkDesktopController {

    @Autowired
    WorkDesktopService workDesktopService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @RequestMapping("/approval")
    @ApiOperation("查询个人待审核事件")
    public List<ApprovalVO> listApproval(@ApiParam(value = "组织id", required = true)
                                         @PathVariable("organization_id") Long organizationId,
                                         @ApiParam(value = "项目id")
                                         @RequestParam(value = "project_id", required = false) Long projectId) {
        return workDesktopService.listApproval(organizationId, projectId);
    }
}
