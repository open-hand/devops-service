package io.choerodon.devops.infra.util

import org.mockito.ArgumentMatchers
import org.powermock.api.mockito.PowerMockito
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.core.exception.CommonException
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.mapper.AppServiceMapper

/**
 *
 * @author zmf
 * @since 12/11/19
 *
 */
@Subject(MapperUtil)
@Stepwise
class MapperUtilSpec extends Specification {
    private AppServiceMapper normalMapper = PowerMockito.mock(AppServiceMapper)
    private AppServiceMapper abnormalMapper = PowerMockito.mock(AppServiceMapper)
    private static final String ERROR_INSERT = "error.insert"
    private static final String ERROR_UPDATE = "error.update"

    def setup() {
        PowerMockito.when(normalMapper.insert(ArgumentMatchers.any(AppServiceDTO))).thenReturn(1)
        PowerMockito.when(normalMapper.insertSelective(ArgumentMatchers.any(AppServiceDTO))).thenReturn(1)
        PowerMockito.when(normalMapper.updateByPrimaryKey(ArgumentMatchers.any(AppServiceDTO))).thenReturn(1)
        PowerMockito.when(normalMapper.updateByPrimaryKeySelective(ArgumentMatchers.any(AppServiceDTO))).thenReturn(1)

        PowerMockito.when(abnormalMapper.insert(ArgumentMatchers.any(AppServiceDTO))).thenReturn(0)
        PowerMockito.when(abnormalMapper.insertSelective(ArgumentMatchers.any(AppServiceDTO))).thenReturn(0)
        PowerMockito.when(abnormalMapper.updateByPrimaryKey(ArgumentMatchers.any(AppServiceDTO))).thenReturn(0)
        PowerMockito.when(abnormalMapper.updateByPrimaryKeySelective(ArgumentMatchers.any(AppServiceDTO))).thenReturn(0)
    }

    def "ResultJudgedInsert"() {
        given: "准备"
        AppServiceDTO appServiceDTO = new AppServiceDTO()

        when: "调用正常的结果"
        MapperUtil.resultJudgedInsert(normalMapper, appServiceDTO, ERROR_INSERT)

        then: "校验结果"
        noExceptionThrown()

        when: "调用不正常的结果"
        MapperUtil.resultJudgedInsert(abnormalMapper, appServiceDTO, ERROR_INSERT)

        then: "校验结果"
        thrown(CommonException)
    }

    def "ResultJudgedInsertSelective"() {
        given: "准备"
        AppServiceDTO appServiceDTO = new AppServiceDTO()

        when: "调用正常的结果"
        MapperUtil.resultJudgedInsertSelective(normalMapper, appServiceDTO, ERROR_INSERT)

        then: "校验结果"
        noExceptionThrown()

        when: "调用不正常的结果"
        MapperUtil.resultJudgedInsertSelective(abnormalMapper, appServiceDTO, ERROR_INSERT)

        then: "校验结果"
        thrown(CommonException)

    }

    def "ResultJudgedUpdateByPrimaryKey"() {
        given: "准备"
        AppServiceDTO appServiceDTO = new AppServiceDTO()

        when: "调用正常的结果"
        MapperUtil.resultJudgedUpdateByPrimaryKey(normalMapper, appServiceDTO, ERROR_UPDATE)

        then: "校验结果"
        noExceptionThrown()

        when: "调用不正常的结果"
        MapperUtil.resultJudgedUpdateByPrimaryKey(abnormalMapper, appServiceDTO, ERROR_UPDATE)

        then: "校验结果"
        thrown(CommonException)
    }

    def "ResultJudgedUpdateByPrimaryKeySelective"() {
        given: "准备"
        AppServiceDTO appServiceDTO = new AppServiceDTO()

        when: "调用正常的结果"
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(normalMapper, appServiceDTO, ERROR_UPDATE)

        then: "校验结果"
        noExceptionThrown()

        when: "调用不正常的结果"
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(abnormalMapper, appServiceDTO, ERROR_UPDATE)

        then: "校验结果"
        thrown(CommonException)
    }
}
