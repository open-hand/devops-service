import React, { Component, Fragment, useState, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Choerodon } from '@choerodon/boot';
import { Radio } from 'choerodon-ui/pro';
import {
  Form,
  // Radio,
  Upload,
  Icon,
} from 'choerodon-ui';
import _ from 'lodash';
import classnames from 'classnames';
import InterceptMask from '../../../../../../../components/intercept-mask';
import YamlEditor from '../../../../../../../components/yamlEditor/YamlEditor';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const { Item: FormItem } = Form;
// const { Group: RadioGroup } = Radio;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

const ResourceSidebar = injectIntl(inject('AppState')(observer((props) => {
  const [showError, setShowError] = useState(false);
  const [mode, setMode] = useState('paste');
  const [changedValue, setChangedValue] = useState(null);
  const [hasEditorError, setHasEditorError] = useState(false);
  const [fileDisabled, setFileDisabled] = useState(false);

  useEffect(() => {
    const {
      store,
      AppState: {
        currentMenuType: { projectId },
      },
      type,
      id,
      modal,
    } = props;
    if (id && type === 'edit') {
      store.loadSingleData(projectId, id);
    }
    modal.handleOk(handleSubmit);
    return () => {
      store.setSingleData({});
    };
  }, [changedValue]);

  const handleSubmit = async () => {
    const {
      form: { validateFields },
      store,
      type,
      AppState: { currentMenuType: { projectId } },
      envId: propsEnv,
      refresh,
    } = props;
    if (hasEditorError) {
      return false;
    }
    if (mode === 'paste' && !changedValue) {
      setShowError(true);
      return false;
    }
    let result = true;
    const formData = new FormData();
    if (type === 'edit') {
      const { getSingleData: { id, envId } } = store;
      const data = {
        envId: envId || propsEnv,
        type: 'update',
        content: changedValue,
        resourceId: id,
      };
      _.forEach(data, (value, key) => formData.append(key, value));
    } else {
      validateFields((err, data) => {
        result = !err;
        if (!err) {
          const { file } = data;
          formData.append('envId', propsEnv);
          formData.append('type', 'create');
          if (mode === 'paste') {
            formData.append('content', changedValue);
          } else {
            formData.append('contentFile', file.file);
          }
        }
      });
    }
    if (!result) {
      return false;
    }
    try {
      const res = await store.createData(projectId, formData);
      if (handlePromptError(res, false)) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  };

  const checkFile = (rule, value, callback) => {
    const { intl: { formatMessage } } = props;
    if (!value) {
      callback(formatMessage({ id: 'resource.required' }));
    } else {
      const { file: { name }, fileList } = value;
      if (!fileList.length) {
        callback(formatMessage({ id: 'resource.required' }));
      } else if (!name.endsWith('.yml') && !name.endsWith('.yaml')) {
        callback(formatMessage({ id: 'file.type.error' }));
      } else if (fileList.length > 1) {
        callback(formatMessage({ id: 'resource.one.file' }));
      } else {
        callback();
      }
    }
  };

  const beforeUpload = () => {
    setFileDisabled(true);
    return false;
  };

  const removeFile = () => {
    setFileDisabled(false);
  };

  /**
   * 切换添加模式
   * @param e
   */
  const changeMode = (value, oldValue) => {
    const { modal } = props;
    const modeCurrent = value;
    setChangedValue(null);
    setHasEditorError(false);
    setShowError(false);
    setFileDisabled(false);
    setMode(modeCurrent);

    modal.update({ okProps: { disabled: false } });
  };

  const handleChangeValue = (value) => {
    setChangedValue(value);
    setShowError(false);
  };

  const handleEnableNext = (flag) => {
    const { modal } = props;
    setHasEditorError(flag);
    modal.update({ okProps: { disabled: flag } });
  };

  const {
    type,
    form: { getFieldDecorator },
    intl: { formatMessage },
    AppState: { currentMenuType: { name } },
    store,
  } = props;

  const {
    getSingleData: {
      name: resourceName,
      resourceContent,
    },
  } = store;

  const uploadClass = classnames({
    'c7ncd-upload-select': !fileDisabled,
    'c7ncd-upload-disabled': fileDisabled,
  });

  return (
    <div className="c7n-region c7ncd-deployment-resource-sidebar">
      <Form layout="vertical">
        {type === 'create' && (<Fragment>
          <div className="c7ncd-resource-mode">
            <div className="c7ncd-resource-mode-label">{formatMessage({ id: 'resource.mode' })}：</div>
            {
              _.map(['paste', 'upload'], (item) => (
                <Radio
                  name="addMode"
                  key={item}
                  value={item}
                  checked={mode === item}
                  onChange={changeMode}
                  className="c7ncd-resource-radio"
                >
                  {formatMessage({ id: `resource.mode.${item}` })}
                </Radio>
              ))
            }
          </div>
          </Fragment>)}
        {mode === 'paste' && (
          <YamlEditor
            readOnly={false}
            value={changedValue || resourceContent || ''}
            originValue={resourceContent || ''}
            onValueChange={handleChangeValue}
            handleEnableNext={handleEnableNext}
          />
        )}
        {mode === 'upload' && (
          <FormItem {...formItemLayout} className="c7ncd-resource-upload-item">
            {getFieldDecorator('file', {
              rules: [{
                validator: checkFile,
              }],
            })(
              <Upload
                // action="//jsonplaceholder.typicode.com/posts/"
                accept=".yml, .yaml"
                disabled={fileDisabled}
                beforeUpload={beforeUpload}
                onRemove={removeFile}
              >
                <div className={uploadClass}>
                  <Icon
                    className="c7ncd-resource-upload-icon"
                    type="add"
                  />
                  <div className="c7n-resource-upload-text">Upload</div>
                </div>
              </Upload>,
            )}
          </FormItem>
        )}
      </Form>
      {showError && <div className="c7ncd-resource-error">
        <FormattedMessage id="contentCanNotBeEmpty" />
      </div>}
    </div>
  );
})));

export default Form.create()(ResourceSidebar);
