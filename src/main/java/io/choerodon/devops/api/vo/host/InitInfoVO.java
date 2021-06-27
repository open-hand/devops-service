package io.choerodon.devops.api.vo.host;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 21:05
 */
public class InitInfoVO {
    private List<JavaProcessInfoVO> javaProcessInfos;
    private List<DockerProcessInfoVO> dockerProcessInfos;

    public List<JavaProcessInfoVO> getJavaProcessInfos() {
        return javaProcessInfos;
    }

    public void setJavaProcessInfos(List<JavaProcessInfoVO> javaProcessInfos) {
        this.javaProcessInfos = javaProcessInfos;
    }

    public List<DockerProcessInfoVO> getDockerProcessInfos() {
        return dockerProcessInfos;
    }

    public void setDockerProcessInfos(List<DockerProcessInfoVO> dockerProcessInfos) {
        this.dockerProcessInfos = dockerProcessInfos;
    }
}
