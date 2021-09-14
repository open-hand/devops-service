package io.choerodon.devops.api.controller.v1;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DeploymentInfoVO;
import io.choerodon.devops.api.vo.InstanceControllerDetailVO;
import io.choerodon.devops.api.vo.WorkloadBaseCreateOrUpdateVO;
import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.app.service.impl.DevopsDeploymentServiceImpl;
import io.choerodon.devops.infra.enums.DeploymentSourceTypeEnums;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/deployments")
public class DevopsDeploymentController {

    @Autowired
    WorkloadService workloadService;

    @Autowired
    private DevopsDeploymentService devopsDeploymentService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "通过粘贴yaml/或上传文件形式创建或更新deployment资源")
    @PostMapping
    public void createOrUpdate(@PathVariable(value = "project_id") Long projectId,
                               @ModelAttribute @Valid WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO,
                               BindingResult bindingResult,
                               @RequestParam(value = "contentFile", required = false) MultipartFile contentFile) {
        if (bindingResult.hasErrors()) {
            throw new CommonException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_SOURCE_TYPE, DeploymentSourceTypeEnums.WORKLOAD.getType());
        workloadBaseCreateOrUpdateVO.setExtraInfo(extraInfo);
        workloadService.createOrUpdate(projectId, workloadBaseCreateOrUpdateVO, contentFile, ResourceType.DEPLOYMENT, false);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除deployment资源")
    @DeleteMapping
    public void delete(@PathVariable(value = "project_id") Long projectId,
                       @RequestParam @Encrypt Long id) {
        workloadService.delete(projectId, id, ResourceType.DEPLOYMENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询deployment列表")
    @GetMapping("/paging")
    public ResponseEntity<Page<DeploymentInfoVO>> pagingByEnvId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "env_id") @Encrypt Long envId,
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "from_instance", required = false) Boolean fromInstance
    ) {
        return ResponseEntity.ok(devopsDeploymentService.pagingByEnvId(projectId, envId, pageable, name, fromInstance));
    }

    /**
     * 根据deploymentId获取更多部署详情(Yaml格式)
     *
     * @param projectId    项目id
     * @param deploymentId deployment id
     * @return 部署详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据deploymentId获取更多部署详情(Yaml格式)")
    @GetMapping(value = "/{deployment_id}/detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getDeploymentDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "deployment_id") Long deploymentId) {
        return new ResponseEntity<>(devopsDeploymentService.getInstanceResourceDetailYaml(deploymentId), HttpStatus.OK);
    }


    /**
     * 根据deploymentId获取更多部署详情(Json格式)
     *
     * @param projectId    项目id
     * @param deploymentId deployment id
     * @return 部署详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据deploymentId获取更多部署详情(Json格式)")
    @GetMapping(value = "/{deployment_id}/detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getDeploymentDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "deployment_id") Long deploymentId) {
        return new ResponseEntity<>(devopsDeploymentService.getInstanceResourceDetailJson(deploymentId), HttpStatus.OK);
    }

    /**
     * 停止deployment资源(数量变为0)
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "停止deployment资源(pod数量变为0)")
    @PutMapping(value = "/{deployment_id}/stop")
    public ResponseEntity<Void> stop(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "deployment_id") Long deploymentId
    ) {
        devopsDeploymentService.stopDeployment(projectId, deploymentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 启用deployment资源(数量变为1)
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "启用deployment资源(pod数量变为1)")
    @PutMapping(value = "/{deployment_id}/start")
    public ResponseEntity<Void> start(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "deployment_id") Long deploymentId
    ) {
        devopsDeploymentService.startDeployment(projectId, deploymentId);
        return ResponseEntity.noContent().build();
    }
}
