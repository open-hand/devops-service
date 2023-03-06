package io.choerodon.devops.infra.feign;

import java.util.List;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import com.cdancy.jenkins.rest.domain.job.Workflow;
import com.cdancy.jenkins.rest.filters.JenkinsAuthenticationFilter;
import com.cdancy.jenkins.rest.parsers.OptionalFolderPathParser;
import org.jclouds.Fallbacks;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.ParamParser;
import org.jclouds.rest.annotations.RequestFilters;

@RequestFilters(JenkinsAuthenticationFilter.class)
@Path("/")
public interface C7nJobsApi {

    // below four apis are for "pipeline-stage-view-plugin",
    // see https://github.com/jenkinsci/pipeline-stage-view-plugin/tree/master/rest-api
    @Named("jobs:run-lastBuild")
    @Path("{optionalFolderPath}job/{name}/choerodon/lastBuild")
    @Fallback(Fallbacks.NullOnNotFoundOr404.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    List<Workflow> runHistory(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                              @PathParam("name") String jobName);


}
