package io.choerodon.devops.infra.feign.fallback;

import java.util.Map;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import io.choerodon.devops.api.dto.AppVersionAndValueDTO;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.infra.feign.AppShareClient;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/7/2
 * Description:
 */
@Component
public class AppShareClientFallback implements AppShareClient {
    @Override
    public Call<PageInfo<ApplicationReleasingDTO>> getAppShares(String accessToken, Map<String, Object> map) {
        return null;
    }

    @Override
    public Call<PageInfo<ApplicationVersionRepDTO>> listVersionByAppId(Long appId, Map<String, Object> map) {
        return null;
    }

    @Override
    public Call<AppVersionAndValueDTO> getConfigInfoByVerionId(Long appId, Long versionId, Map<String, Object> map) {
        return null;
    }
}
