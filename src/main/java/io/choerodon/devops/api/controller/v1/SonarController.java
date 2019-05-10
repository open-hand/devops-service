package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.SonarUserDTO;
import io.choerodon.devops.app.service.SonarService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:00 2019/5/8
 * Description:
 */
@RestController
@RequestMapping(value = "/sonar")
public class SonarController {
    @Autowired
    private SonarService sonarService;

    /**
     * 用于获取sonar Admin用户
     *
     * @return
     */
    @ApiOperation(value = "触发自动部署")
    @GetMapping("/info")
    public ResponseEntity<SonarUserDTO> getSonarInfo() {
        return Optional.ofNullable(sonarService.getSonarInfo())
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.sonar.info"));
    }

}
