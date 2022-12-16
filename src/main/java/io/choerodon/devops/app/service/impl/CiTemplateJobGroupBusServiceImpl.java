package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.enums.CiTemplateJobGroupTypeEnum;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateJobGroupBusServiceImpl implements CiTemplateJobGroupBusService {

    @Autowired
    private CiTemplateJobGroupBusMapper ciTemplateJobGroupBusMapper;

    @Autowired
    private CiTemplateJobMapper ciTemplateJobMapper;

    @Override
    public Page<CiTemplateJobGroupVO> pageTemplateJobGroup(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateJobGroupVO> ciTemplateJobGroupVOS = PageHelper.doPageAndSort(pageRequest,
                () -> ciTemplateJobGroupBusMapper.queryTemplateJobGroupByParams(sourceId, searchParam));

        UserDTOFillUtil.fillUserInfo(ciTemplateJobGroupVOS.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        ciTemplateJobGroupVOS.getContent().forEach(ciTemplateJobGroupVO -> {
            if (ciTemplateJobGroupVO.getBuiltIn()) {
                ciTemplateJobGroupVO.setCreator(null);
            }
        });
        return ciTemplateJobGroupVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobGroupVO createTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO) {
        if (!checkTemplateJobGroupName(sourceId, ciTemplateJobGroupVO.getName(), null)) {
            throw new CommonException("error.job.group.exist");
        }
        ciTemplateJobGroupVO.setBuiltIn(false);
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ConvertUtils
                .convertObject(ciTemplateJobGroupVO, CiTemplateJobGroupDTO.class);
        ciTemplateJobGroupDTO.setType(CiTemplateJobGroupTypeEnum.OTHER.value());
        ciTemplateJobGroupBusMapper.insert(ciTemplateJobGroupDTO);
        return ConvertUtils.convertObject(ciTemplateJobGroupDTO, CiTemplateJobGroupVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobGroupVO updateTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO) {

        if (!checkTemplateJobGroupName(sourceId, ciTemplateJobGroupVO.getName(), ciTemplateJobGroupVO.getId())) {
            throw new CommonException("error.job.group.exist");
        }
        CiTemplateJobGroupDTO templateJobGroupDTO
                = ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobGroupVO.getId());
        AssertUtils.notNull(templateJobGroupDTO, "error.ci.job.template.group.not.exist");
        AssertUtils.isTrue(!templateJobGroupDTO.getBuiltIn(), "error.update.builtin.job.template.group");

        ciTemplateJobGroupVO.setBuiltIn(false);
        BeanUtils.copyProperties(ciTemplateJobGroupVO, templateJobGroupDTO);
        ciTemplateJobGroupBusMapper.updateByPrimaryKeySelective(templateJobGroupDTO);
        return ConvertUtils.convertObject(templateJobGroupDTO, CiTemplateJobGroupVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateJobGroup(Long sourceId, Long ciTemplateJobGroupId) {
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobGroupId);
        if (ciTemplateJobGroupDTO == null) {
            return;
        }
        checkRelated(ciTemplateJobGroupDTO);
        AssertUtils.isTrue(!ciTemplateJobGroupDTO.getBuiltIn(), "error.delete.builtin.job.template.group");

        ciTemplateJobGroupBusMapper.deleteByPrimaryKey(ciTemplateJobGroupId);
    }

    @Override
    public Boolean checkTemplateJobGroupName(Long sourceId, String name, Long templateJobId) {
        return ciTemplateJobGroupBusMapper.checkTemplateJobGroupName(sourceId, name, templateJobId) != null ?
                Boolean.FALSE : Boolean.TRUE;
    }


    @Override
    public Page<CiTemplateJobGroupVO> pageTemplateJobGroupByCondition(Long sourceId, PageRequest pageRequest) {
        Page<CiTemplateJobGroupVO> ciTemplateJobGroupVOS = pageTemplateJobGroup(sourceId, pageRequest, null);
        if (CollectionUtils.isEmpty(ciTemplateJobGroupVOS.getContent())) {
            return ciTemplateJobGroupVOS;
        }
        //其他分类没有任务 则其他类型在创建流水线的时候不显示
//        ciTemplateJobGroupVOS.getContent().stream().filter(ciTemplateJobGroupVO -> StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType,"")).

        return null;
    }

    @Override
    public List<CiTemplateJobGroupVO> listTemplateJobGroup(Long sourceId, String name) {
        List<CiTemplateJobGroupVO> ciTemplateJobGroupVOS
                = ciTemplateJobGroupBusMapper.queryTemplateJobGroupByParams(sourceId, name);
        if (!CollectionUtils.isEmpty(ciTemplateJobGroupVOS)) {
            List<CiTemplateJobGroupVO> templateJobGroupVOS = sortedTemplateJob(ciTemplateJobGroupVOS);
            return templateJobGroupVOS;
        }
        return Collections.emptyList();
    }

    private List<CiTemplateJobGroupVO> sortedTemplateJob(List<CiTemplateJobGroupVO> ciTemplateJobGroupVOS) {
        List<CiTemplateJobGroupVO> resultTemplateJobGroupVOS = new ArrayList<>();
        List<CiTemplateJobGroupVO> templateJobGroupVOS = ciTemplateJobGroupVOS.stream()
                .sorted(Comparator.comparing(CiTemplateJobGroupVO::getId).reversed()).collect(Collectors.toList());
        //构建放在第一位 自定义的放在最后
        List<CiTemplateJobGroupVO> customJobGroupVOS = templateJobGroupVOS.stream()
                .filter(ciTemplateJobGroupVO -> !ciTemplateJobGroupVO.getBuiltIn()).collect(Collectors.toList());
        List<CiTemplateJobGroupVO> otherVos = templateJobGroupVOS.stream()
                .filter(CiTemplateJobGroupVO::getBuiltIn)
                .filter(ciTemplateJobGroupVO -> StringUtils
                        .equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.OTHER.value()))
                .collect(Collectors.toList());


        List<CiTemplateJobGroupVO> firstVos = templateJobGroupVOS
                .stream()
                .filter(ciTemplateJobGroupVO -> StringUtils
                        .equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.BUILD.value()))
                .collect(Collectors.toList());

        List<CiTemplateJobGroupVO> groupVOS = templateJobGroupVOS.stream().filter(CiTemplateJobGroupVO::getBuiltIn)
                .filter(ciTemplateJobGroupVO ->
                        !StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.OTHER.value()))
                .filter(ciTemplateJobGroupVO ->
                        !StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.BUILD.value()))
                .sorted(Comparator.comparing(CiTemplateJobGroupVO::getId))
                .collect(Collectors.toList());

        resultTemplateJobGroupVOS.addAll(firstVos);
        resultTemplateJobGroupVOS.addAll(groupVOS);
        resultTemplateJobGroupVOS.addAll(otherVos);
        resultTemplateJobGroupVOS.addAll(customJobGroupVOS);
        return resultTemplateJobGroupVOS;
    }

    /**
     * 校验组织层或者平台层是否有关联该分类
     *
     * @param ciTemplateJobGroupDTO
     */
    private void checkRelated(CiTemplateJobGroupDTO ciTemplateJobGroupDTO) {
        CiTemplateJobDTO ciTemplateJobDTO = new CiTemplateJobDTO();
        ciTemplateJobDTO.setGroupId(ciTemplateJobGroupDTO.getId());
        List<CiTemplateJobDTO> ciTemplateStepDTOS = ciTemplateJobMapper.select(ciTemplateJobDTO);
        AssertUtils.isTrue(CollectionUtils.isEmpty(ciTemplateStepDTOS), "error.delete.job.group.related");
    }
}
