package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:57
 */
public class MavenbuildTemplateVO {
    @ApiModelProperty("步骤名称")
    @NotEmpty(message = "error.step.name.cannot.be.null")
    private String name;

    @ApiModelProperty("步骤顺序")
    @NotNull(message = "error.setp.sequence.cannot.be.null")
    private Long sequence;

    @ApiModelProperty("执行脚本")
    @NotEmpty(message = "error.step.script.cannot.be.null")
    private String script;

    @ApiModelProperty("Maven的依赖仓库")
    private List<MavenRepoVO> repos;

    @JsonIgnore
    private Boolean hasSettings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public List<MavenRepoVO> getRepos() {
        return repos;
    }

    public void setRepos(List<MavenRepoVO> repos) {
        this.repos = repos;
    }

    public Boolean getHasSettings() {
        return hasSettings;
    }

    public void setHasSettings(Boolean hasSettings) {
        this.hasSettings = hasSettings;
    }
}
