package io.choerodon.devops.api.vo.jenkins;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:29
 */
public class JenkinsInputParameterDef {
    @ApiModelProperty("Jenkins input 参数类型")
    private String type;
    @ApiModelProperty("Jenkins input 参数名称")
    private String name;
    @ApiModelProperty("Jenkins input 参数描述")
    private String description;
    @ApiModelProperty("Jenkins input 参数详情")
    private Map<String, Object> definition;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getDefinition() {
        return definition;
    }

    public void setDefinition(Map<String, Object> definition) {
        this.definition = definition;
    }
}
