import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Choerodon } from '@choerodon/boot';
import {
  Form,
  Modal,
  Radio,
  Upload,
  Icon,
  Button,
  Select,
  Tooltip,
} from 'choerodon-ui';
import _ from 'lodash';
import classnames from 'classnames';
import InterceptMask from '../../../../../../../components/intercept-mask';
import YamlEditor from '../../../../../../../components/yamlEditor/YamlEditor';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const { Sidebar } = Modal;
const { Option } = Select;
const { Item: FormItem } = Form;
const { Group: RadioGroup } = Radio;
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

@Form.create({})
@injectIntl
@inject('AppState')
@observer
export default class ResourceSidebar extends Component {
  state = {
    showError: false,
    mode: 'paste',
    changedValue: null,
    hasEditorError: false,
    fileDisabled: false,
  };

  componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: { projectId },
      },
      type,
      id,
      modal,
    } = this.props;
    if (id && type === 'edit') {
      store.loadSingleData(projectId, id);
    }
    modal.handleOk(this.handleSubmit);
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setSingleData({});
  }

  handleSubmit = async () => {
    const {
      form: { validateFields },
      store,
      type,
      AppState: { currentMenuType: { projectId } },
      envId: propsEnv,
      refresh,
    } = this.props;
    const {
      hasEditorError,
      changedValue,
      mode,
    } = this.state;
    if (hasEditorError) {
      return false;
    }
    if (mode === 'paste' && !changedValue) {
      this.setState({ showError: true });
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

  checkFile = (rule, value, callback) => {
    const { intl: { formatMessage } } = this.props;
    if (!value) {
      callback(formatMessage({ id: 'resource.required' }));
    } else {
      const { file: { name }, fileList } = value;
      if (!fileList.length) {
        callback(formatMessage({ id: 'resource.required' }));
      } else if (!name.endsWith('.yml')) {
        callback(formatMessage({ id: 'file.type.error' }));
      } else if (fileList.length > 1) {
        callback(formatMessage({ id: 'resource.one.file' }));
      } else {
        callback();
      }
    }
  };

  beforeUpload = () => {
    this.setState({ fileDisabled: true });
    return false;
  };

  removeFile = () => {
    this.setState({ fileDisabled: false });
  };

  /**
   * 切换添加模式
   * @param e
   */
  changeMode = (e) => {
    const { modal } = this.props;
    const mode = e.target.value;
    this.setState({
      changedValue: null,
      hasEditorError: false,
      showError: false,
      mode,
    });
    modal.update({ okProps: { disabled: false } });
  };

  handleChangeValue = (value) => {
    this.setState({ changedValue: value, showError: false });
  };

  handleEnableNext = (flag) => {
    const { modal } = this.props;
    this.setState({ hasEditorError: flag });
    modal.update({ okProps: { disabled: flag } });
  };

  render() {
    const {
      type,
      form: { getFieldDecorator },
      intl: { formatMessage },
      AppState: { currentMenuType: { name } },
      store,
    } = this.props;
    const {
      changedValue,
      fileDisabled,
      mode,
      showError,
    } = this.state;
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
              <RadioGroup
                value={mode}
                onChange={this.changeMode}
              >
                {
                  _.map(['paste', 'upload'], (item) => (
                    <Radio
                      key={item}
                      value={item}
                      className="c7ncd-resource-radio"
                    >
                      {formatMessage({ id: `resource.mode.${item}` })}
                    </Radio>
                  ))
                }
              </RadioGroup>
            </div>
          </Fragment>)}
          {mode === 'paste' && (
            <YamlEditor
              readOnly={false}
              value={changedValue || resourceContent || ''}
              originValue={resourceContent || ''}
              onValueChange={this.handleChangeValue}
              handleEnableNext={this.handleEnableNext}
            />
          )}
          {mode === 'upload' && (
            <FormItem {...formItemLayout} className="c7ncd-resource-upload-item">
              {getFieldDecorator('file', {
                rules: [{
                  validator: this.checkFile,
                }],
              })(
                <Upload
                  // action="//jsonplaceholder.typicode.com/posts/"
                  disabled={fileDisabled}
                  beforeUpload={this.beforeUpload}
                  onRemove={this.removeFile}
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
  }
}
