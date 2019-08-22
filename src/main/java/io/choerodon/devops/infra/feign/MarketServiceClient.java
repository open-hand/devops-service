package io.choerodon.devops.infra.feign;

import java.util.List;

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
    @POST("v1/market_applications/upload")
    Call<Void> uploadFile(@Query("app_version") String appVersion, @Query("image_url") String imageUrl, @Part List<MultipartBody.Part> list);

    @GET("{fileName}")
    Call<ResponseBody> downloadFile(@Path("fileName") String fileName);
}