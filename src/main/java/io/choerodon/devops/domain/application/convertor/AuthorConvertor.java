package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.AuthorDTO;
import io.choerodon.devops.infra.dataobject.gitlab.AuthorDO;

@Component
public class AuthorConvertor implements ConvertorI<Object, AuthorDO, AuthorDTO> {

    @Override
    public AuthorDTO doToDto(AuthorDO dataObject) {
        AuthorDTO authorDTO = new AuthorDTO();
        BeanUtils.copyProperties(dataObject, authorDTO);
        return authorDTO;
    }

    @Override
    public AuthorDO dtoToDo(AuthorDTO dto) {
        AuthorDO authorDO = new AuthorDO();
        BeanUtils.copyProperties(dto, authorDO);
        return authorDO;
    }


}
