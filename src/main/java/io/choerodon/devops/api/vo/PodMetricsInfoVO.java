package io.choerodon.devops.api.vo;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/9/9.
 */
public class PodMetricsInfoVO {

    @SerializedName("Items")
    private List<PodMetricsItemVO> items;


    public List<PodMetricsItemVO> getItems() {
        return items;
    }

    public void setItems(List<PodMetricsItemVO> items) {
        this.items = items;
    }

}
