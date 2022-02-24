package io.choerodon.devops.infra.feign;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * Created by Sheep on 2019/5/6.
 */
public interface NexusClient {

    /**
     * 查询jar版本的metadata文件，只有snapshot才有多个版本，才有这个metadata文件
     *
     * @param repositoryName        仓库名
     * @param slashSeparatedGroupId /分隔的groupId，例如: io/choerodon
     * @param version               版本号
     * @return xml文件
     */
    @GET("repository/{repositoryName}/{slashSeparatedGroupId}/{version}/maven-metadata.xml")
    Call<String> componentMetadata(@Path("repositoryName") String repositoryName,
                                   @Path("slashSeparatedGroupId") String slashSeparatedGroupId,
                                   @Path("version") String version);

    /**
     * 查询jar版本的metadata文件，只有snapshot才有多个版本，才有这个metadata文件
     *
     * @param slashSeparatedGroupId /分隔的groupId，例如: io/choerodon
     * @param version               版本号
     * @return xml文件
     */
    @GET("/{slashSeparatedGroupId}/{version}/maven-metadata.xml")
    Call<String> customComponentMetadata(@Path("slashSeparatedGroupId") String slashSeparatedGroupId,
                                         @Path("version") String version);
}
