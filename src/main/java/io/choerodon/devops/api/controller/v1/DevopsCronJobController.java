package io.choerodon.devops.api.controller.v1;

import java.util.Objects;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.WorkloadBaseCreateOrUpdateVO;
import io.choerodon.devops.api.vo.workload.CronJobInfoVO;
import io.choerodon.devops.app.service.DevopsCronJobService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/cron_jobs")
public class DevopsCronJobController {
    @Autowired
    private DevopsCronJobService devopsCronJobService;
    @Autowired
    private WorkloadService workloadService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询cronjobs列表")
    @GetMapping("/paging")
    public ResponseEntity<Page<CronJobInfoVO>> pagingByEnvId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "env_id") @Encrypt Long envId,
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "from_instance", required = false) Boolean fromInstance
    ) {
        return ResponseEntity.ok(devopsCronJobService.pagingByEnvId(projectId, envId, pageable, name, fromInstance));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "通过粘贴yaml/或上传文件形式创建或更新cronjjob资源")
    @PostMapping
    public void createOrUpdate(@PathVariable(value = "project_id") Long projectId,
                               @ModelAttribute @Valid WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO,
                               BindingResult bindingResult,
                               @RequestParam(value = "contentFile", required = false) MultipartFile contentFile) {
        if (bindingResult.hasErrors()) {
            throw new CommonException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        workloadService.createOrUpdate(projectId, workloadBaseCreateOrUpdateVO, contentFile, ResourceType.CRON_JOB);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除cronjob资源")
    @DeleteMapping
    public void delete(@PathVariable(value = "project_id") Long projectId,
                       @RequestParam @Encrypt Long id) {
        workloadService.delete(projectId, id, ResourceType.CRON_JOB);
    }
}
