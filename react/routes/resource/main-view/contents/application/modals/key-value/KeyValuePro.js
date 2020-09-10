import React, { Fragment, useState, useEffect, useMemo, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';
import { Choerodon } from '@choerodon/boot';
import { Form, DataSet, TextField, TextArea } from 'choerodon-ui/pro';
import { Button, Icon, Table, Tooltip } from 'choerodon-ui';
import { objToYaml, yamlToObj, takeObject, ConfigNode, makePostData } from '../utils';
import YamlEditor from '../../../../../../../components/yamlEditor';
import { handlePromptError } from '../../../../../../../utils';
import { useKeyValueStore } from './stores';

import '../../../../../../main.less';
import './index.less';

const FormView = observer(() => {
  const {
    id,
    appId,
    store,
    intl: { formatMessage },
    AppState: {
      currentMenuType: {
        projectId,
        name: menuName,
      },
    },
    title,
    modeSwitch,
    intlPrefix,
    envId,
    refresh,
    modal,
    FormDataSet,
    KeyValueDataSet,
  } = useKeyValueStore();

  const [dataYaml, setDataYaml] = useState('');
  const [hasItemError, setHasItemError] = useState(false);
  const [warningMes, setWarningMes] = useState('');
  const [isYamlEdit, setIsYamlEdit] = useState(false);
  const [hasYamlError, setHasYamlError] = useState(false);
  const [hasValueError, setHasValueError] = useState(false);
  const [valueErrorMsg, setValueErrorMsg] = useState('');

  const [isSubmit, setIsSubmit] = useState(false);


  useEffect(() => {
    async function callBack() {
      if (id) {
        const res = await FormDataSet.query();
        const dataSourceCurrent = _.map(res.value, (value, key) => new ConfigNode(key, value));
        KeyValueDataSet.loadData(dataSourceCurrent);
      }
    }
    callBack();
  }, []);

  useEffect(() => {
    checkButtonDisabled(isSubmit);
  }, [hasValueError, hasItemError, hasYamlError]);

  /**
   * 删除key-value
   * @param key
   */
  const handleDelete = (record) => {
    KeyValueDataSet.remove(record);
    asyncCheckErrorData(KeyValueDataSet.toData());
  };

  /**
   * 添加一组 key/value
   * @param data
   */
  const handleAdd = () => {
    KeyValueDataSet.create();
  };

  /**
   * configMap 规则中value只能是字符串
   * @param data
   */
  const checkConfigRuleError = (dataCurrent = '') => {
    const yaml = dataCurrent || dataYaml;
    const yamlObj = yamlToObj(yaml) || {};
    const values = Object.values(yamlObj);

    let error = false;
    for (let i = 0, len = values.length; i < len; i++) {
      if (typeof values[i] !== 'string' || values[i] === '') {
        error = true;
        break;
      }
    }

    setHasValueError(error);
    return error;
  };

  const asyncCheckConfigRuleError = _.debounce(checkConfigRuleError, 600);

  /**
   * 同步校验键值对
   * @param data
   * @returns {boolean}
   */
  const checkErrorData = (dataCurrent = null, isSubmitCurrent = false) => {
    const dataSource = KeyValueDataSet.toData();
    const _data = dataCurrent || dataSource;
    const hasKey = _data.filter(({ key }) => !_.isEmpty(key));
    const onlyHasValue = _data.filter(({ key, value }) => _.isEmpty(key) && !_.isEmpty(value));
    const onlyHasKey = hasKey.filter(({ value }) => _.isEmpty(value));
    const hasErrorItem = onlyHasKey.length || onlyHasValue.length;
    const hasRepeatKey = hasKey.length !== _.uniqBy(hasKey, 'key').length;
    const hasEmptyKey = title === 'cipher' && (_.isEmpty(hasKey) || hasKey.length !== _data.length);

    let hasErrorKey;
    // eslint-disable-next-line no-restricted-syntax
    for (const { key } of hasKey) {
      // eslint-disable-next-line no-useless-escape
      const pattern = /[^0-9A-Za-z\.\-\_]/;
      if (pattern.test(key)) {
        hasErrorKey = true;
        break;
      }
    }

    if (!(hasErrorItem || hasErrorKey || hasRepeatKey || hasEmptyKey)) {
      setWarningMes('');
      setIsSubmit(isSubmitCurrent);
      setHasItemError(false);
      return false;
    }

    const errorMsg = hasErrorKey ? '键值不能含空格及-、_、.以外的特殊字符，请检查输入' : formatMessage({
      id: hasRepeatKey ? 'configMap.keyRepeat' : `${title}.keyValueSpan`,
    });

    setConfigError(errorMsg);

    return true;
  };

  /**
   * 校验键值对
   * @param data
   * @returns {boolean}
   */
  const asyncCheckErrorData = _.debounce(checkErrorData, 500);

  /**
   * 设置键值对模式下的错误提示
   * @param msg
   */
  function setConfigError(msg) {
    setWarningMes(msg);
    setHasItemError(true);
    modal.update({ okProps: { disabled: true } });
  }

  const formValidate = async () => {
    let configData = [];
    let hasKVError = false;
    let hasConfigRuleError = false;

    return new Promise((resolve) => {
      const { name, description } = FormDataSet.toData()[0];
      const dataSource = KeyValueDataSet.toData();
      if (!isYamlEdit) {
        hasKVError = checkErrorData(null, true);
        const allData = [...dataSource.filter(item => !_.isEmpty(item.key))];
        configData = allData;
      } else {
        hasConfigRuleError = checkConfigRuleError();
        configData = yamlToObj(dataYaml);
      }

      if (hasYamlError || hasKVError || hasConfigRuleError) {
        resolve(false);
      } else {
        setHasItemError(false);
        const _value = takeObject(configData);
        const dto = {
          name,
          description,
          envId,
          appServiceId: appId,
          type: id ? 'update' : 'create',
          id: id || undefined,
          value: _value,
        };
        resolve(dto);
      }
    });
  };

  /**
   * form提交函数
   * 添加粘贴后key-value校验
   * @param e
   */
  const handleSubmit = async () => {
    const isValidate = await FormDataSet.validate();
    if (isValidate) {
      const postData = await formValidate();
      if (!postData) {
        return false;
      }
      try {
        const res = await store.postKV(projectId, postData);
        if (handlePromptError(res)) {
          refresh();
          return true;
        } else {
          return false;
        }
      } catch (error) {
        Choerodon.handleResponseError(error);
        return false;
      }
    } else {
      return false;
    }
  };

  const getDataSourceMapFormItem = () => KeyValueDataSet.data.map(record => (
    <Form record={record} key={record.id}>
      <div className="c7n-config-container">
        <TextField
          style={{
            width: 190,
          }}
          name="key"
          onBlur={() => checkErrorData(null, false)}
          placeholder="键"
        />
        <span className="c7n-config-equal">=</span>
        <TextArea
          className="c7n-config-value"
          name="value"
          rows={1}
          placeholder="值"
          onBlur={() => checkErrorData(null, false)}
        />
        <Icon
          className="del-btn"
          type="delete"
          onClick={() => handleDelete(record)}
        />
      </div>
    </Form>
  ));

  /**
   * 编辑 configMap 组件节点
   * 有两种模式：key/value编辑模式、YAML代码编辑模式
   * @returns {*}
   */
  const getConfigMap = () => {
    let configMap = null;
    if (!isYamlEdit) {
      configMap = <Fragment>
        {getDataSourceMapFormItem()}
        <Button icon="add" onClick={handleAdd} type="primary">
          <FormattedMessage id={`${intlPrefix}.${title}.add`} />
        </Button>
        {hasItemError ? <div className="c7n-cm-warning">{warningMes}</div> : null}
      </Fragment>;
    } else {
      configMap = <Fragment>
        <YamlEditor
          readOnly={false}
          modeChange={false}
          value={dataYaml}
          onValueChange={changeYamlValue}
          handleEnableNext={checkYamlError}
        />
        <div className="c7ncd-config-yaml-tip">{hasValueError && (valueErrorMsg
          || <FormattedMessage id="configMap.yaml.error" />)}</div>
      </Fragment>;
    }

    return configMap;
  };

  /**
   * yaml 值改变
   * @param value
   */
  const changeYamlValue = (value) => {
    asyncCheckConfigRuleError(value);

    setDataYaml(value);
  };

  /**
   * 校验yaml格式
   * @param flag
   */
  const checkYamlError = (flag) => {
    setHasYamlError(flag);
  };

  /**
   * 切换配置映射的编辑模式
   */
  const changeEditMode = () => {
    if (hasYamlError || hasValueError || hasItemError) return;

    if (!isYamlEdit) {
      const dataSource = KeyValueDataSet.toData();
      const result = checkErrorData(dataSource);

      if (result) return;

      const yamlValue = objToYaml(dataSource);

      checkConfigRuleError(yamlValue);

      setHasItemError(false);
      setIsYamlEdit(true);
      setWarningMes('');
      setDataYaml(yamlValue);
    } else {
      const result = checkConfigRuleError(dataYaml);

      if (result) return;

      try {
        const kvValue = yamlToObj(dataYaml);
        const postData = makePostData(kvValue);

        KeyValueDataSet.loadData(postData);
        setHasYamlError(false);
        setIsYamlEdit(false);
        setDataYaml('');
      } catch (e) {
        setHasValueError(true);
        setValueErrorMsg(e.message);
        modal.update({ okProps: { disabled: true } });
      }
    }
  };

  const checkButtonDisabled = (isSubmitCurrent = false) => {
    !isSubmitCurrent && modal.update({ okProps: { disabled: hasYamlError || hasValueError || hasItemError } });
    setIsSubmit(false);
  };

  const disableBtn = hasYamlError || hasValueError || hasItemError;

  modal.handleOk(handleSubmit);

  return (
    <div className="c7n-region">
      <div>
        <Form dataSet={FormDataSet} className="c7n-sidebar-form" layout="vertical">
          <TextField
            name="name"
            // autoFocus={!id}
            disabled={!!id}
          />
          <TextArea
            name="description"
            // autoFocus={!!id}
            autosize={{ minRows: 2 }}
          />
        </Form>
        <div className="c7n-sidebar-from-title">
          <FormattedMessage id={`${intlPrefix}.${title}.head`} />
          {!isYamlEdit && <Tooltip
            overlayStyle={{ maxWidth: 350 }}
            title={formatMessage({ id: `${intlPrefix}.${title}.help.tooltip` })}
          >
            <Icon type="help" />
          </Tooltip>}
          {modeSwitch ? <Button
            className="c7n-config-mode-btn"
            type="primary"
            funcType="flat"
            disabled={disableBtn}
            onClick={changeEditMode}
          >
            <FormattedMessage id={isYamlEdit ? 'configMap.mode.yaml' : 'configMap.mode.kv'} />
          </Button> : null}
        </div>
        <div className="c7n-config-editor">
          {getConfigMap()}
        </div>
      </div>
    </div>
  );
});
export default FormView;
