package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Created by younger on 2018/4/10.
 */
public class AppServiceUpdateDTO {

    private Long id;
    private String name;

    private List<DevopsConfigVO> devopsConfigVOS;

    public List<DevopsConfigVO> getDevopsConfigVOS() {
        return devopsConfigVOS;
    }

    public void setDevopsConfigVOS(List<DevopsConfigVO> devopsConfigVOS) {
        this.devopsConfigVOS = devopsConfigVOS;
    }

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


}
