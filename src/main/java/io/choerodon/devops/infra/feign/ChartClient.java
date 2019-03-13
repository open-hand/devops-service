package io.choerodon.devops.infra.feign;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChartClient {


    @GET("health")
    Call<Object> getHealth();

    @GET("{orgCode}/{proCode}/charts/{appCode}-{appVersion}.tgz")
    Call<ResponseBody> getTaz(@Path("orgCode") String orgCode, @Path("proCode") String proCode, @Path("appCode") String appCode, @Path("appVersion") String appVersion);


}
