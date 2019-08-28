package io.choerodon.devops.api.vo;


import java.util.List;

/**
 * @author zhaotianxin
 * @since 2019/8/13
 */
public class AppServiceGroupVO {
    private Long id;
    private String name;
    private String code;
    private Boolean share;
    private List<AppServiceGroupInfoVO> appServiceList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getShare() {
        return share;
    }

    public void setShare(Boolean share) {
        this.share = share;
    }

    public List<AppServiceGroupInfoVO> getAppServiceList() {
        return appServiceList;
    }

    public void setAppServiceList(List<AppServiceGroupInfoVO> appServiceList) {
        this.appServiceList = appServiceList;
    }

}
