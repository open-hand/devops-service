package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;

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
     * @return baseList of DevopsEnvFileErrorDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询环境文件错误列表")
    @GetMapping(value = "/list_by_env")
    public ResponseEntity<List<DevopsEnvFileErrorVO>> listByEnvId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvFileService.listByEnvId(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.fileError.baseList"));
    }

    /**
     * 项目下分页查询环境文件错误列表
     *
     * @param projectId 项目 ID
     * @param envId     环境 ID
     * @return baseList of DevopsEnvFileErrorDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下分页查询环境文件错误列表")
    @CustomPageRequest
    @GetMapping(value = "/page_by_env")
    public ResponseEntity<Page<DevopsEnvFileErrorVO>> pageByEnvId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "环境 ID", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvFileService.pageByEnvId(envId, pageable))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.fileError.baseList"));
    }
}
