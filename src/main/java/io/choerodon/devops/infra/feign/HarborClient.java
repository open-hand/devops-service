package io.choerodon.devops.infra.feign;

import java.util.Map;

import io.choerodon.devops.infra.dataobject.harbor.*;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PutMapping;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.*;

public interface HarborClient {
    @GET("api/users/current")
    Call<User> getCurrentUser();

    @GET("api/projects")
    Call<List<ProjectDetail>> listProject(@Query("name") String name);

    @GET("api/projects/{project_id}/members")
    Call<Object> listProjectMember(@Path("project_id") Long projectId);

    @POST("api/projects")
    Call<Void> insertProject(@Body Project harborProject);

    @POST("api/users")
    Call<Void> insertUser(@Body User harborUser);

    @POST("api/projects/{project_id}/members")
    Call<Void> setProjectMember(@Path("project_id") Integer projectId, @Body Role role);

    @POST("api/projects/{project_id}/members")
    Call<Void> setProjectMember(@Path("project_id") Integer projectId, @Body ProjectMember projectMember);

    @POST("api/projects")
    Call<Void> insertProject(@QueryMap Map<String,String> entityName, @Body Project harborProject);

    @GET("api/users")
    Call<List<User>> listUser(@Query("username") String username);

    @PUT("api/projects/{project_id}")
    Call<Void> updateProject(@Path("project_id") Integer projectId, @Body ProjectDetail harborProject);

    @GET("api/systeminfo")
    Call<SystemInfo> getSystemInfo();

    @DELETE("api/projects/{project_id}/members/{mid}")
    Call<Void> deleteMember(@Path("project_id") Integer projectId, @Path("mid") Integer memberId);

    @DELETE("api/projects/{project_id}/members/{user_id}")
    Call<Void> deleteLowVersionMember(@Path("project_id") Integer projectId, @Path("user_id") Integer userId);

    @GET("api/projects/{project_id}/members")
    Call<List<ProjectMember>> getProjectMembers(@Path("project_id")Integer projectId, @Query("entityname") String entityname);
}
