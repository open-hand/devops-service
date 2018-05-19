package io.choerodon.devops.infra.feign;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import io.choerodon.devops.infra.dataobject.harbor.Project;
import io.choerodon.devops.infra.dataobject.harbor.User;

public interface HarborClient {
    @GET("api/users/current")
    Call<User> getCurrentUser();

    @Headers({"Content-Type:application/json;charset=UTF-8", "Accept:application/json"})
    @POST("api/projects")
    Call<Object> insertProject(@Body Project harborProject);
}
