package io.choerodon.devops.infra.feign;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.notify.SendSettingDTO;
import io.choerodon.devops.infra.feign.fallback.NotifyServiceClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by Sheep on 2019/5/15.
 */

@FeignClient(value = "notify-service", path = "/v1/notices", fallback = NotifyServiceClientFallBack.class)
public interface NotifyClient {

    @PostMapping
    void sendMessage(@RequestBody NoticeSendDTO dto);

    @GetMapping("/send_settings/codes/{code}")
    ResponseEntity<SendSettingDTO> queryByCode(@PathVariable("code") String code);
}
