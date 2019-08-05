package io.choerodon.devops.api.vo.kubernetes;

public class CheckLog {
    private String content;
    private String result;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "CheckLog{" +
                "content='" + content + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
