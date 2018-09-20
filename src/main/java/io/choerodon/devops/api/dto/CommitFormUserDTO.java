package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 17:15
 * Description:
 */
public class CommitFormUserDTO {
    private String name;
    private String imgUrl;
    private List<Date> commitDates;

    public CommitFormUserDTO(String name, String imgUrl, List<Date> commitDates) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.commitDates = commitDates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<Date> getCommitDates() {
        return commitDates;
    }

    public void setCommitDates(List<Date> commitDates) {
        this.commitDates = commitDates;
    }
}
