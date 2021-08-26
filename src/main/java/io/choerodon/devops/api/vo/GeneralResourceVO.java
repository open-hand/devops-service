package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * 资源概览VO
 *
 * @author xingxingwu.hand-china.com 2021/07/13 17:02
 */
public class GeneralResourceVO {
    Integer hostTotal = 0;
    List<ResourceGroup> hostGroups;

    Integer clusterTotal = 0;
    List<ResourceGroup> clusterGroups;

    Integer envTotal = 0;
    List<ResourceGroup> envGroups;


    /**
     * 设计这个ResourceGroup的目的是未来会先通过项目分组，然后项目下通过资源池分组，保证数据结构的通用性所以设计这玩意
     */
    public static class ResourceGroup {
        String groupName;
        Integer count;
        boolean leafFlag;
        List<ResourceGroup> hostGroups;

        public ResourceGroup(String groupName, Integer count, boolean leafFlag) {
            this.groupName = groupName;
            this.count = count;
            this.leafFlag = leafFlag;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public boolean isLeafFlag() {
            return leafFlag;
        }

        public void setLeafFlag(boolean leafFlag) {
            this.leafFlag = leafFlag;
        }

        public List<ResourceGroup> getHostGroups() {
            return hostGroups;
        }

        public void setHostGroups(List<ResourceGroup> hostGroups) {
            this.hostGroups = hostGroups;
        }
    }

    public Integer getHostTotal() {
        return hostTotal;
    }

    public void setHostTotal(Integer hostTotal) {
        this.hostTotal = hostTotal;
    }

    public List<ResourceGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<ResourceGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public Integer getClusterTotal() {
        return clusterTotal;
    }

    public void setClusterTotal(Integer clusterTotal) {
        this.clusterTotal = clusterTotal;
    }

    public List<ResourceGroup> getClusterGroups() {
        return clusterGroups;
    }

    public void setClusterGroups(List<ResourceGroup> clusterGroups) {
        this.clusterGroups = clusterGroups;
    }

    public Integer getEnvTotal() {
        return envTotal;
    }

    public void setEnvTotal(Integer envTotal) {
        this.envTotal = envTotal;
    }

    public List<ResourceGroup> getEnvGroups() {
        return envGroups;
    }

    public void setEnvGroups(List<ResourceGroup> envGroups) {
        this.envGroups = envGroups;
    }
}
