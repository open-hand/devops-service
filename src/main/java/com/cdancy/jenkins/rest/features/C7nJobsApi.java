/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cdancy.jenkins.rest.features;

import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.cdancy.jenkins.rest.binders.BindMapToForm;
import com.cdancy.jenkins.rest.domain.common.RequestStatus;
import com.cdancy.jenkins.rest.domain.common.Response;
import com.cdancy.jenkins.rest.domain.job.C7nBuildInfo;
import com.cdancy.jenkins.rest.filters.JenkinsAuthenticationFilter;
import com.cdancy.jenkins.rest.parsers.CustomFallback;
import com.cdancy.jenkins.rest.parsers.CustomResponseParser;
import com.cdancy.jenkins.rest.parsers.OptionalFolderPathParser;
import com.cdancy.jenkins.rest.parsers.RequestStatusParser;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.rest.annotations.*;

@RequestFilters(JenkinsAuthenticationFilter.class)
@Path("/")
public interface C7nJobsApi {


    @Named("jobs:lastBuild")
    @Path("{optionalFolderPath}job/{name}/choerodon/lastBuild")
    @Fallback(CustomFallback.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    C7nBuildInfo lastBuild(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                           @PathParam("name") String jobName);

    @Named("jobs:buildHisttory")
    @Path("{optionalFolderPath}job/{name}/choerodon/buildHistory")
    @Fallback(CustomFallback.class)
    @ResponseParser(CustomResponseParser.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    Response buildHistory(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                          @PathParam("name") String jobName);

    @Named("jobs:buildHisttory")
    @Path("{optionalFolderPath}job/{name}/{number}/choerodon/buildInfo")
    @Fallback(CustomFallback.class)
    @ResponseParser(CustomResponseParser.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    Response buildInfo(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                       @PathParam("name") String jobName,
                       @PathParam("number") int buildNumber);

    @Named("jobs:restart")
    @Path("{optionalFolderPath}job/{name}/{number}/choerodon/restart")
    @Fallback(CustomFallback.class)
    @ResponseParser(CustomResponseParser.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    Response restart(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                     @PathParam("name") String jobName,
                     @PathParam("number") int buildNumber);


    @Named("jobs:inputSubmit")
    @Path("{optionalFolderPath}job/{name}/{number}/wfapi/inputSubmit")
    @Fallback(CustomFallback.class)
    @ResponseParser(RequestStatusParser.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    RequestStatus inputSubmit(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                              @PathParam("name") String jobName,
                              @PathParam("number") int buildNumber,
                              @QueryParam("inputId") String inputId,
                              @Nullable @BinderParam(BindMapToForm.class) Map<String, List<String>> properties);

    @Named("jobs:abortInput")
    @Path("{optionalFolderPath}job/{name}/{number}/input/{inputId}/abort")
    @Fallback(CustomFallback.class)
    @ResponseParser(RequestStatusParser.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    RequestStatus abort(@Nullable @PathParam("optionalFolderPath") @ParamParser(OptionalFolderPathParser.class) String optionalFolderPath,
                        @PathParam("name") String jobName,
                        @PathParam("number") int buildNumber,
                        @PathParam("inputId") String inputId);

}
