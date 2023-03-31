package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.LinkedHashMap;
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
    @ApiModelProperty("Url to include external yaml from")
    private String include;

    @ApiModelProperty("The image for jobs")
    private String image;

    @ApiModelProperty("stage is defined per-job and relies on stages which is defined globally. It allows to group jobs into different stages, and jobs of the same stage are executed in parallel (subject to certain conditions).")
    private List<String> stages;

    private Map<String, String> variables;

    // 用linkedHashMap的原因是保证job的遍历顺序
    @YamlUnwrapped
    @JsonUnwrapped
    @ApiModelProperty("Job")
    private LinkedHashMap<String, CiJob> jobs;

    @YamlProperty(value = "before_script")
    @JsonProperty("before_script")
    @ApiModelProperty("before_script")
    private List<String> beforeScript;

    @YamlProperty(value = "default")
    @JsonProperty("default")
    private Map<String, Object> defaultSection;

    public Map<String, Object> getDefaultSection() {
        return defaultSection;
    }

    public void setDefaultSection(Map<String, Object> defaultSection) {
        this.defaultSection = defaultSection;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
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

    public LinkedHashMap<String, CiJob> getJobs() {
        return jobs;
    }

    public void setJobs(LinkedHashMap<String, CiJob> jobs) {
        this.jobs = jobs;
    }

    @JsonAnySetter
    public void addJob(String name, CiJob ciJob) {
        if (jobs == null) {
            jobs = new LinkedHashMap<>();
        }
        jobs.put(name, ciJob);
    }

    public List<String> getBeforeScript() {
        return beforeScript;
    }

    public void setBeforeScript(List<String> beforeScript) {
        this.beforeScript = beforeScript;
    }
}
