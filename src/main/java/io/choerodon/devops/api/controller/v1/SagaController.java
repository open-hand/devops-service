//package io.choerodon.devops.api.controller.v1;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import io.choerodon.devops.app.eventhandler.SagaHandler;
//import io.choerodon.swagger.annotation.Permission;
//
///**
// * Creator: ChangpingShi0213@gmail.com
// * Date:  9:16 2019/4/15
// * Description:
// */
//@RestController
//@RequestMapping(value = "/saga")
//public class SagaController {
//
//    @Autowired
//    private SagaHandler sagaHandler;
//
//    @Permission(permissionPublic = true)
//    @PostMapping
//    public ResponseEntity<Void> getAppDeployStatusTask(@RequestBody String json) {
//        sagaHandler.deleteChartTags(json);
//        return ResponseEntity.noContent().build();
//    }
//}
