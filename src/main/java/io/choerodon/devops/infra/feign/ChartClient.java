package io.choerodon.devops.infra.feign;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ChartClient {


    @GET("health")
    Call<Object> getHealth();

    /**
     * 用于获取ChartMuseum首页以校验用户名密码的正确性
     * @return 首页的html内容
     */
    @GET("/")
    Call<Void> getHomePage();

    @GET("{orgCode}/{proCode}/charts/{appServiceCode}-{appServiceVersion}.tgz")
    Call<ResponseBody> downloadTaz(@Path("orgCode") String orgCode, @Path("proCode") String proCode, @Path("appServiceCode") String appServiceCode, @Path("appServiceVersion") String appServiceVersion);

    @Multipart
    @POST("{orgCode}/{proCode}/api/charts")
    Call<Object> uploadTaz(@Path("orgCode") String orgCode, @Path("proCode") String proCode, @Part MultipartBody.Part file);


}
