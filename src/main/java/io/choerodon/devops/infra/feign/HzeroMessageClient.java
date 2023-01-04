package io.choerodon.devops.infra.feign;

import org.hzero.common.HZeroService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.infra.feign.fallback.HzeroMessageServiceClientFallBack;

/**
 * Created by Sheep on 2019/5/15.
 */

@FeignClient(value = HZeroService.Message.NAME, fallback = HzeroMessageServiceClientFallBack.class)
public interface HzeroMessageClient {
    @GetMapping("/choerodon/v1/projects/{project_id}/message_settings/type/{notify_type}/code/{code}")
    MessageSettingVO queryByEnvIdAndEventNameAndProjectIdAndCode(
            @PathVariable("notify_type") String notifyType,
            @PathVariable("project_id") Long projectId,
            @PathVariable("code") String code,
            @RequestParam("env_id") Long envId,
            @RequestParam("event_name") String eventName);
}
