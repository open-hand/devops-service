package io.choerodon.devops.api.controller.v1;

import com.github.pagehelper.PageInfo;
import com.google.j2objc.annotations.AutoreleasePool;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.annotation.Target;
import java.util.Optional;


@RestController
@RequestMapping("/v1/projects/{project_id}/pv")
public class DevopsPvController {

    @Autowired
    DevopsPvServcie devopsPvServcie;

    /***
     * 查询项目下所有pv
     * @param projectId
     * @return
     */
    @GetMapping("/querypv")
    public ResponseEntity<PageInfo<DevopsPvVO>> queryAll(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return Optional.ofNullable(devopsPvServcie.queryAll(pageRequest))
                .map(target -> new ResponseEntity(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("pv.is.null"));
    }

    @PostMapping("/createpv")
    public ResponseEntity createpv(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @RequestBody DevopsPvDTO devopsPvDTO){
        devopsPvServcie.createPv(devopsPvDTO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }





}
