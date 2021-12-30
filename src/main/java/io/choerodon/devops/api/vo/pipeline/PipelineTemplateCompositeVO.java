package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.PipelineTemplateVO;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/1 14:31
 */
public class PipelineTemplateCompositeVO {

    @ApiModelProperty("模板的语言集合")
    private List<CiTemplateCategoryDTO> ciTemplateCategoryDTOList;
    @ApiModelProperty("模板集合")
    private List<PipelineTemplateVO> pipelineTemplateVOList;

    public PipelineTemplateCompositeVO(List<CiTemplateCategoryDTO> ciTemplateCategoryDTOList, List<PipelineTemplateVO> pipelineTemplateVOList) {
        this.ciTemplateCategoryDTOList = ciTemplateCategoryDTOList;
        this.pipelineTemplateVOList = pipelineTemplateVOList;
    }

    public List<CiTemplateCategoryDTO> getCiTemplateCategoryDTOList() {
        return ciTemplateCategoryDTOList;
    }

    public void setCiTemplateCategoryDTOList(List<CiTemplateCategoryDTO> ciTemplateCategoryDTOList) {
        this.ciTemplateCategoryDTOList = ciTemplateCategoryDTOList;
    }

    public List<PipelineTemplateVO> getPipelineTemplateVOList() {
        return pipelineTemplateVOList;
    }

    public void setPipelineTemplateVOList(List<PipelineTemplateVO> pipelineTemplateVOList) {
        this.pipelineTemplateVOList = pipelineTemplateVOList;
    }
}
