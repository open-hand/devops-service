package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.api.ws.gitops.AgentGitOpsSocketHandler;
import io.choerodon.swagger.annotation.Permission;

/**
 * 用于在Pod退出前的回调接口，用于清理这个pod所持有的一些资源
 *
 * @author zmf
 * @since 11/15/19
 */
@RestController
@RequestMapping("/pre_stop")
public class PreStopController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreStopController.class);

    @Autowired
    private AgentGitOpsSocketHandler agentGitOpsSocketHandler;

    /**
     * 释放这个微服务实例所持有的一些资源(比如redis的键)
     * 一般由外部的容器根据生命周期调用
     */
    @PostMapping
    @Permission(permissionWithin = true)
    @ApiOperation(value = "释放这个微服务实例所持有的一些资源(比如redis的键),一般由外部的容器根据生命周期调用")
    public ResponseEntity<Void> preStop() {
        LOGGER.info("PreStop API is being called...");
        agentGitOpsSocketHandler.removeRedisKeyOfThisMicroService();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
