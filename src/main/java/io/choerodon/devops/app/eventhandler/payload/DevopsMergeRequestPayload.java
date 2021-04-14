package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsMergeRequestVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;

public class DevopsMergeRequestPayload {
    private AppServiceDTO appServiceDTO;
    private DevopsMergeRequestVO devopsMergeRequestVO;

    public AppServiceDTO getAppServiceDTO() {
        return appServiceDTO;
    }

    public void setAppServiceDTO(AppServiceDTO appServiceDTO) {
        this.appServiceDTO = appServiceDTO;
    }

    public DevopsMergeRequestVO getDevopsMergeRequestVO() {
        return devopsMergeRequestVO;
    }

    public void setDevopsMergeRequestVO(DevopsMergeRequestVO devopsMergeRequestVO) {
        this.devopsMergeRequestVO = devopsMergeRequestVO;
    }
}
