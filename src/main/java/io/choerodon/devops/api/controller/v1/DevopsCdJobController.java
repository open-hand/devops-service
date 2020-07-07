//package io.choerodon.devops.api.controller.v1;
//
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import io.choerodon.core.iam.InitRoleCode;
//import io.choerodon.core.iam.ResourceLevel;
//import io.choerodon.devops.app.service.DevopsCdJobService;
//import io.choerodon.swagger.annotation.Permission;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2020/7/6 15:00
// */
//@RestController
//@RequestMapping("/v1/projects/{project_id}/cd_jobs")
//public class DevopsCdJobController {
//
//    @Autowired
//    private DevopsCdJobService devopsCdJobService;
//
//
//    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
//    @ApiOperation(value = "查询job日志")
//    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/trace")
//    public ResponseEntity<String> queryTrace(
//            @PathVariable(value = "project_id") Long projectId,
//            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
//            @PathVariable(value = "job_id") Long jobId) {
//        return ResponseEntity.ok(devopsCdJobService.queryTrace(gitlabProjectId, jobId));
//    }
//
//    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
//    @ApiOperation(value = "重试job")
//    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/retry")
//    public ResponseEntity<Void> retryJob(
//            @PathVariable(value = "project_id") Long projectId,
//            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
//            @PathVariable(value = "job_id") Long jobId) {
//        devopsCdJobService.retryJob(projectId, gitlabProjectId, jobId);
//        return ResponseEntity.noContent().build();
//    }
//}
