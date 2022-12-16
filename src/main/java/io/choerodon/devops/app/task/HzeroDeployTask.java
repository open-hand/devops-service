//package io.choerodon.devops.app.task;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.devops.app.service.DevopsDeployService;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2021/8/12 15:12
// */
//
//@Component
//public class HzeroDeployTask {
//
//    @Autowired
//    private DevopsDeployService devopsDeployService;
//
//    /**
//     * 更新hzero部署状态， 20 分钟执行一次
//     */
//    @Scheduled(cron = "0 0/20 * * * ?")
//    public void updateStatus(){
//        devopsDeployService.updateStatus();
//    }
//}
// hzero相关功能暂时不可用，注释减小开销
