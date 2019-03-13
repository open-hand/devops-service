package io.choerodon.devops.infra.feign;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ChartClient {


    @GET("health")
    Call<Object> getHealth();

}
