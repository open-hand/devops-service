package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.PushWebHookVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;

public class DevopsGitlabTagPayload {
    private AppServiceDTO appServiceDTO;
    private PushWebHookVO pushWebHookVO;

    public AppServiceDTO getAppServiceDTO() {
        return appServiceDTO;
    }

    public void setAppServiceDTO(AppServiceDTO appServiceDTO) {
        this.appServiceDTO = appServiceDTO;
    }

    public PushWebHookVO getPushWebHookVO() {
        return pushWebHookVO;
    }

    public void setPushWebHookVO(PushWebHookVO pushWebHookVO) {
        this.pushWebHookVO = pushWebHookVO;
    }
}
