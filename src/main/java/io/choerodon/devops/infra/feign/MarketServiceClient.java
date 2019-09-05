package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  12:06 2019/8/18
 * Description:
 */
public interface MarketServiceClient {

    @Multipart
    @POST("market/v1/market_applications/upload")
    Call<ResponseBody> uploadFile(@Query("remote_token") String remoteToken,
                                  @Query("app_version") String appVersion,
                                  @Part List<MultipartBody.Part> list,
                                  @Part("imageUrl") String imageUrl);

    @Multipart
    @PUT("market/v1/market_applications/published/versionFix")
    Call<ResponseBody> updateAppPublishInfoFix(@Query("remote_token") String remoteToken,
                                               @Query("app_code") String code,
                                               @Query("version") String version,
                                               @Part("marketApplicationVOStr") String marketApplicationVOStr,
                                               @Part List<MultipartBody.Part> list,
                                               @Part("imageUrl") String imageUrl);

    @GET("{fileName}")
    Call<ResponseBody> downloadFile(@Path(value = "fileName") String fileName, @QueryMap(encoded = true) Map<String, String> map);
}