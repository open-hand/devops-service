package io.choerodon.devops.infra.feign;

import java.util.Map;

import com.github.pagehelper.PageInfo;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.iam.MarketAppDeployRecordDTO;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:41 2019/7/2
 * Description:
 */
public interface AppShareClient {

    @GET("v1/public/app_publishes/by_token")
    Call<PageInfo<RemoteApplicationServiceVO>> getAppShares(@QueryMap Map<String, Object> map);

    @GET("v1/public/app_publishes/{app_id}/list_versions")
    Call<PageInfo<MarketAppPublishVersionVO>> listVersionByAppId(@Path("app_id") Long appId,
                                                                @QueryMap Map<String, Object> map);

    @GET("v1/public/app_publishes/{app_id}/versions/{version_id}/config_info")
    Call<AppVersionAndValueVO> getConfigInfoByVerionId(@Path("app_id") Long appId,
                                                       @Path("version_id") Long versionId,
                                                       @QueryMap Map<String, Object> map);

    @GET("v1/public/check_token")
    Call<AccessTokenCheckResultVO> checkTokenExist(@Query("access_token") String accessToken);


    @POST("v1/public/app_deploy_records")
    Call<Void> createAppDeployRecord(@Query("access_token") String accessToken, @Body MarketAppDeployRecordDTO marketAppDeployRecordDTO);

}
