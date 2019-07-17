package io.choerodon.devops.infra.feign;

import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.devops.api.vo.ApplicationVersionRespVO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import io.choerodon.devops.api.vo.AccessTokenCheckResultDTO;
import io.choerodon.devops.api.vo.AppVersionAndValueDTO;
import io.choerodon.devops.api.vo.ApplicationReleasingDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:41 2019/7/2
 * Description:
 */
public interface AppShareClient {

    @POST("v1/public/app_shares/by_token")
    Call<PageInfo<ApplicationReleasingDTO>> getAppShares(@QueryMap Map<String, Object> map);

    @POST("v1/public/app_shares/{app_id}/list_versions")
    Call<PageInfo<ApplicationVersionRespVO>> listVersionByAppId(@Path("app_id") Long appId,
                                                                @QueryMap Map<String, Object> map);

    @GET("v1/public/app_shares/{app_id}/versions/{version_id}/config_info")
    Call<AppVersionAndValueDTO> getConfigInfoByVerionId(@Path("app_id") Long appId,
                                                        @Path("version_id") Long versionId,
                                                        @QueryMap Map<String, Object> map);

    @POST("v1/public/app_shares/check_token")
    Call<AccessTokenCheckResultDTO> checkTokenExist(@Query("access_token") String accessToken);

}
