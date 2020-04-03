package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.annotation.YamlProperty;
import io.choerodon.devops.infra.annotation.YamlUnwrapped;

/**
 * 文档链接  https://docs.gitlab.com/ee/ci/yaml/README.html
 *
 * @author zmf
 * @since 20-4-2
 */
public class GitlabCi {
    @ApiModelProperty("Using the include keyword, you can allow the inclusion of external YAML files")
    private List<Include> include;

    @ApiModelProperty("The image for jobs")
    private String image;

    @ApiModelProperty("stage is defined per-job and relies on stages which is defined globally. It allows to group jobs into different stages, and jobs of the same stage are executed in parallel (subject to certain conditions).")
    private List<String> stages;

    @YamlUnwrapped
    @JsonUnwrapped
    @ApiModelProperty("Job")
    private Map<String, Job> jobs;

    @YamlProperty(value = "before_script")
    @JsonProperty("before_script")
    @ApiModelProperty("before_script")
    private List<String> beforeScript;


    public List<Include> getInclude() {
        return include;
    }

    public void setInclude(List<Include> include) {
        this.include = include;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getStages() {
        return stages;
    }

    public void setStages(List<String> stages) {
        this.stages = stages;
    }

    public Map<String, Job> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, Job> jobs) {
        this.jobs = jobs;
    }

    @JsonAnySetter
    public void addJob(String name, Job job) {
        if (jobs == null) {
            jobs = new HashMap<>();
        }
        jobs.put(name, job);
    }

    public List<String> getBeforeScript() {
        return beforeScript;
    }

    public void setBeforeScript(List<String> beforeScript) {
        this.beforeScript = beforeScript;
    }
}
