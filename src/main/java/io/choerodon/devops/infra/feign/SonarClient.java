package io.choerodon.devops.infra.feign;

import java.util.Map;

import io.choerodon.devops.api.vo.sonar.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by Sheep on 2019/5/6.
 */
public interface SonarClient {

    @GET("api/measures/component")
    Call<SonarComponent> getSonarComponent(@QueryMap Map<String, String> maps);

    @GET("api/project_analyses/search")
    Call<SonarAnalyses> getAnalyses(@QueryMap Map<String, String> maps);

    @GET("api/issues/search")
    Call<Bug> getBugs(@QueryMap Map<String, String> maps);

    @GET("api/issues/search")
    Call<Vulnerability> getVulnerability(@QueryMap Map<String, String> maps);

    @GET("api/issues/search")
    Call<Bug> getNewBugs(@QueryMap Map<String, String> maps);

    @GET("api/issues/search")
    Call<Vulnerability> getNewVulnerability(@QueryMap Map<String, String> maps);

    @GET("api/measures/search_history")
    Call<SonarTables> getSonarTables(@QueryMap Map<String, String> maps);

    @POST("api/projects/update_visibility")
    Call<Void> updateVisibility(@QueryMap Map<String, String> maps);

    @POST("api/projects/update_default_visibility")
    Call<Void> updateDefaultVisibility(@QueryMap Map<String, String> maps);

    @POST("api/permissions/add_group_to_template")
    Call<Void> addGroupToTemplate(@QueryMap Map<String, String> maps);

    @POST("api/permissions/remove_group_from_template")
    Call<Void> removeGroupFromTemplate(@QueryMap Map<String, String> maps);

    @GET("api/components/search_projects?ps=50")
    Call<Projects> listProject();

    @GET("api/users/current")
    Call<Void> getUser();

    @POST("api/user_tokens/generate")
    Call<ResponseBody> createToken(@QueryMap Map<String, String> maps);
}
