package io.choerodon.devops.infra.feign;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.api.vo.notify.SendSettingDTO;
import io.choerodon.devops.infra.feign.fallback.NotifyServiceClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Sheep on 2019/5/15.
 */

@FeignClient(value = "notify-service", fallback = NotifyServiceClientFallBack.class)
public interface NotifyClient {

    @PostMapping("/v1/notices")
    void sendMessage(@RequestBody NoticeSendDTO dto);

    @GetMapping("/v1/notices/send_settings/codes/{code}")
    ResponseEntity<SendSettingDTO> queryByCode(@PathVariable("code") String code);

    @GetMapping("/v1/projects/{project_id}/message_settings/type/{notify_type}/code/{code}")
    MessageSettingVO queryByEnvIdAndEventNameAndProjectIdAndCode(
            @PathVariable("notify_type") String notifyType,
            @PathVariable("project_id") Long projectId,
            @PathVariable("code") String code,
            @RequestParam("env_id") Long envId,
            @RequestParam("event_name") String eventName);


}
