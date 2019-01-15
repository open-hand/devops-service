package io.choerodon.devops.infra.feign;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

import io.choerodon.devops.infra.dataobject.harbor.Project;
import io.choerodon.devops.infra.dataobject.harbor.User;

public interface HarborClient {
    @GET("api/users/current")
    Call<User> getCurrentUser();

    @Headers({"Content-Type:application/json;charset=UTF-8", "Accept:application/json"})
    @POST("api/projects")
    Call<Object> insertProject(@Body Project harborProject);

    @Headers({"Content-Type:application/json;charset=UTF-8", "Accept:application/json"})
    @POST("api/projects")
    Call<Object> insertProject(@QueryMap Map<String,String> entityName, @Body Project harborProject);
}
