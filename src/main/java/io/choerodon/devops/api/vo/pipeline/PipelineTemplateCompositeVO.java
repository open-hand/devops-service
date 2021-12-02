package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.PipelineTemplateVO;
import io.choerodon.devops.infra.dto.CiTemplateLanguageDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/1 14:31
 */
public class PipelineTemplateCompositeVO {

    @ApiModelProperty("模板的语言集合")
    private List<CiTemplateLanguageDTO> ciTemplateLanguageDTOList;
    @ApiModelProperty("模板集合")
    private List<PipelineTemplateVO> pipelineTemplateVOList;

    public PipelineTemplateCompositeVO(List<CiTemplateLanguageDTO> ciTemplateLanguageDTOList, List<PipelineTemplateVO> pipelineTemplateVOList) {
        this.ciTemplateLanguageDTOList = ciTemplateLanguageDTOList;
        this.pipelineTemplateVOList = pipelineTemplateVOList;
    }

    public List<CiTemplateLanguageDTO> getCiTemplateLanguageDTOList() {
        return ciTemplateLanguageDTOList;
    }

    public void setCiTemplateLanguageDTOList(List<CiTemplateLanguageDTO> ciTemplateLanguageDTOList) {
        this.ciTemplateLanguageDTOList = ciTemplateLanguageDTOList;
    }

    public List<PipelineTemplateVO> getPipelineTemplateVOList() {
        return pipelineTemplateVOList;
    }

    public void setPipelineTemplateVOList(List<PipelineTemplateVO> pipelineTemplateVOList) {
        this.pipelineTemplateVOList = pipelineTemplateVOList;
    }
}
