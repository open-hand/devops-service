package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/8/22.
 */
public class DescribeResourceVO {

    private String namespace;
    private String kind;
    private String name;
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
