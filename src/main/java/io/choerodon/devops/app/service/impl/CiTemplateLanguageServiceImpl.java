package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateLanguageService;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.devops.infra.mapper.CiTemplateLanguageMapper;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */
@Service
public class CiTemplateLanguageServiceImpl implements CiTemplateLanguageService {
    @Autowired
    private CiTemplateLanguageMapper ciTemplateLanguagemapper;

    @Override
    public List<CiTemplateCategoryDTO> listByIds(Set<Long> languageIds) {
        return ciTemplateLanguagemapper.listByIds(languageIds);
    }
}

