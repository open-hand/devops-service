package io.choerodon.devops.api.vo.jenkins;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/8 15:39
 */
public class JenkinsNodeVO {
    private String id;
    private String name;

    private String status;

    private String parameterDescription;

    private String log;


    public JenkinsNodeVO() {
    }

    public JenkinsNodeVO(String id, String name, String status, String parameterDescription, String log) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.parameterDescription = parameterDescription;
        this.log = log;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getParameterDescription() {
        return parameterDescription;
    }

    public void setParameterDescription(String parameterDescription) {
        this.parameterDescription = parameterDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
