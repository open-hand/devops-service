package io.choerodon.devops.api.vo.jenkins;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/6 17:54
 */
public class ParamVO {
    private String name;

    private String value;

    public ParamVO() {
    }

    public ParamVO(String key, String value) {
        this.name = key;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
