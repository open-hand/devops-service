package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:46 2019/8/20
 * Description:
 */
public class AppServiceBatchCheckVO {
    private List<String> listCode;
    private List<String> listName;

    public List<String> getListCode() {
        return listCode;
    }

    public void setListCode(List<String> listCode) {
        this.listCode = listCode;
    }

    public List<String> getListName() {
        return listName;
    }

    public void setListName(List<String> listName) {
        this.listName = listName;
    }
}
