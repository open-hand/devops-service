package io.choerodon.devops.infra.feign;

import java.util.Map;

import io.choerodon.devops.infra.dataobject.harbor.Project;
import retrofit2.Call;
import retrofit2.http.*;

public interface HarborClient {
    @GET("api/users/current")
    Call<Object> getCurrentUser();

    @GET("api/projects")
    Call<Object> listProject(@Query("name") String name);

    @GET("api/projects/{project_id}/members")
    Call<Object> listProjectMember(@Path("project_id") Long projectId);


    @Headers({"Content-Type:application/json;charset=UTF-8", "Accept:application/json"})
    @POST("api/projects")
    Call<Object> insertProject(@Body Project harborProject);

    @Headers({"Content-Type:application/json;charset=UTF-8", "Accept:application/json"})
    @POST("api/projects")
    Call<Object> insertProject(@QueryMap Map<String,String> entityName, @Body Project harborProject);
}
