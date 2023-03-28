package io.choerodon.devops.app.task;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;

/**
 * 每天0点定时遍历证书，发送消息
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:22
 */
@ConditionalOnProperty(value = "local.test", havingValue = "false", matchIfMissing = true)
@Component
//@EnableScheduling
public class CertificationNotifyTask {
    private static final String FIND_AND_SEND_CERTIFICATION_EXPIRE_NOTICE = "findAndSendCertificationExpireNotice";

//    @JobTask(productSource = ZKnowDetailsHelper.VALUE_CHOERODON,
//            maxRetryCount = 3,
//            code = FIND_AND_SEND_CERTIFICATION_EXPIRE_NOTICE,
//            description = "查找将要过期的证书并根据配置发送邮件提醒")
//    @TimedTask(name = FIND_AND_SEND_CERTIFICATION_EXPIRE_NOTICE, description = "查找将要过期的证书并根据配置发送邮件提醒",
//            repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.DAYS, cronExpression = "0 0 0 1 * ?", params = {})
//    public void findAndSendNotice() {
//
//    }

}
