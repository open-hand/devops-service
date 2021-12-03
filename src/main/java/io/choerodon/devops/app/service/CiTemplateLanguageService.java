package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */
public interface CiTemplateLanguageService {

    List<CiTemplateCategoryDTO> listByIds(Set<Long> languageIds);
}

