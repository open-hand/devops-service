package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Sheep on 2019/8/22.
 */
public class DescribeResourceVO {
    @ApiModelProperty("资源所在环境code")
    private String namespace;
    @ApiModelProperty("资源类型")
    private String kind;
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("资源的数据库id")
    private String describeId;


    public DescribeResourceVO(){}


    public DescribeResourceVO(String kind, String name, String env,String describeId){
        this.kind = kind;
        this.name =name;
        this.namespace = env;
        this.describeId = describeId;
    }


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribeId() {
        return describeId;
    }

    public void setDescribeId(String describeId) {
        this.describeId = describeId;
    }
}
