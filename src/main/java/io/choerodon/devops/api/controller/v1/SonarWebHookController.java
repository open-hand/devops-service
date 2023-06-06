package io.choerodon.devops.api.controller.v1;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.SonarAnalyseRecordService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/webhook/sonar")
public class SonarWebHookController {

    @Autowired
    private SonarAnalyseRecordService sonarAnalyseRecordService;

    @Permission(permissionPublic = true)
    @ApiOperation(value = "webhook转发")
    @PostMapping
    public ResponseEntity<Void> saveAnalyseRecord(HttpServletRequest httpServletRequest, @RequestBody String payload) {

        sonarAnalyseRecordService.saveAnalyseRecord(payload, httpServletRequest);
        return ResponseEntity.noContent().build();
    }

}
