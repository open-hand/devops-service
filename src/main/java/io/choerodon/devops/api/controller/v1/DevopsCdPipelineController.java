package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.app.service.DevopsCdPipelineService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 17:24
 */
@RestController
@RequestMapping("/v1/cd_pipeline")
public class DevopsCdPipelineController {

    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;

    /**
     * 启动cd流水线
     * @param token
     * @return
     */
    @Permission(permissionPublic = true)
    @ApiOperation(value = "创建应用服务版本")
    @PostMapping
    public ResponseEntity<Void> triggerCdPipeline(@RequestParam(value = "token") String token,
                                                  @RequestParam(value = "commit") String commit) {
        devopsCdPipelineService.triggerCdPipeline(token, commit);
        return ResponseEntity.ok().build();
    }
}
