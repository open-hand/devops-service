package io.choerodon.devops.infra.feign;

import java.util.Map;

import io.choerodon.devops.api.dto.sonar.Bug;
import io.choerodon.devops.api.dto.sonar.SonarComponent;
import io.choerodon.devops.api.dto.sonar.SonarTables;
import io.choerodon.devops.api.dto.sonar.Vulnerability;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Sheep on 2019/5/6.
 */
public interface SonarClient {

    @GET("api/measures/component")
    Call<SonarComponent> getSonarComponet(@QueryMap Map<String, String> maps);

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
}
