package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

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
    @ApiModelProperty("执行脚本")
    @NotEmpty(message = "error.step.script.cannot.be.null")
    private String script;

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
}
