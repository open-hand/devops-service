package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 10:47
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/envs/{env_id}/error_file")
public class DevopsEnvFileErrorController {
    @Autowired
    private DevopsEnvFileService devopsEnvFileService;

    /**
     * 项目下查询环境文件错误列表
     *
     * @param projectId 项目 ID
     * @param envId     环境 ID
     * @return list of DevopsEnvFileErrorDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询环境文件错误列表")
    @GetMapping(value = "/list")
    public ResponseEntity<List<DevopsEnvFileErrorDTO>> list(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvFileService.listByEnvId(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.fileError.list"));
    }

    /**
     * 项目下查询环境文件错误列表
     *
     * @param projectId 项目 ID
     * @param envId     环境 ID
     * @return list of DevopsEnvFileErrorDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询环境文件错误列表")
    @CustomPageRequest
    @GetMapping(value = "/list_by_page")
    public ResponseEntity<PageInfo<DevopsEnvFileErrorDTO>> page(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "环境 ID", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvFileService.pageByEnvId(envId, pageRequest))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.fileError.list"));
    }
}
