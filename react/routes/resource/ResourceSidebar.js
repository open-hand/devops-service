import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content } from '@choerodon/boot';
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
import InterceptMask from '../../components/interceptMask/InterceptMask';
import YamlEditor from '../../components/yamlEditor/YamlEditor';
import { handlePromptError } from '../../utils';

import '../main.scss';
import './style/ResourceSidebar.scss';

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
    submitting: false,
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
    } = this.props;
    if (id && type === 'edit') {
      store.loadSingleData(projectId, id);
    } else {
      store.loadEnvData(projectId);
    }
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setSingleData({});
  }

  handleSubmit = (e) => {
    e.preventDefault();

    const {
      form: { validateFields },
      store,
      type,
      AppState: { currentMenuType: { projectId } },
      envId: propsEnv,
    } = this.props;
    const {
      hasEditorError,
      changedValue,
      mode,
    } = this.state;
    if (hasEditorError) return;
    this.setState({ submitting: true });
    if (type === 'edit') {
      const { getSingleData: { id, envId } } = store;
      const data = {
        envId: envId || propsEnv,
        type: 'update',
        content: changedValue,
        resourceId: id,
      };
      const formData = new FormData();
      _.forEach(data, (value, key) => formData.append(key, value));
      const promise = store.createData(projectId, formData);
      this.handleResponse(promise);
    } else {
      validateFields((err, data) => {
        if (!err) {
          const formData = new FormData();
          const { envId, file } = data;
          formData.append('envId', envId);
          formData.append('type', 'create');
          if (mode === 'paste') {
            formData.append('content', changedValue);
          } else {
            formData.append('contentFile', file.file);
          }
          const promise = store.createData(projectId, formData);
          this.handleResponse(promise);
        } else {
          this.setState({ submitting: false });
        }
      });
    }
  };

  /**
   * 处理创建修改请求返回的数据
   * @param promise
   */
  handleResponse = (promise) => {
    if (promise) {
      promise
        .then((data) => {
          this.setState({ submitting: false });
          if (handlePromptError(data, false)) {
            this.handleClose(true);
          }
        })
        .catch((e) => {
          this.setState({ submitting: false });
          Choerodon.handleResponseError(e);
        });
    }
  };

  /**
   * 关闭弹框
   */
  handleClose = (reload) => {
    const { onClose } = this.props;
    onClose(reload);
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
    this.setState({
      changedValue: null,
      hasEditorError: false,
      mode: e.target.value,
    });
  };

  handleChangeValue = (value) => {
    this.setState({ changedValue: value });
  };

  handleEnableNext = (flag) => {
    this.setState({ hasEditorError: flag });
  };

  render() {
    const {
      visible,
      type,
      form: { getFieldDecorator },
      intl: { formatMessage },
      AppState: { currentMenuType: { name } },
      store,
      envId,
    } = this.props;
    const {
      submitting,
      changedValue,
      hasEditorError,
      fileDisabled,
      mode,
    } = this.state;
    const {
      getSingleData: {
        name: resourceName,
        resourceContent,
      },
      getEnvData,
    } = store;

    const uploadClass = classnames({
      'c7ncd-upload-select': !fileDisabled,
      'c7ncd-upload-disabled': fileDisabled,
    });

    const envOptions = _.map(getEnvData, ({ connect, id, permission, name: envName }) => {
      const envOptionClass = classnames({
        'c7ncd-status': true,
        'c7ncd-status-success': connect,
        'c7ncd-status-disconnect': !connect,
      });

      return (<Option
        key={id}
        value={id}
        disabled={!permission}
        title={envName}
      >
        <Tooltip title={envName}>
          <span className={envOptionClass} />
          {envName}
        </Tooltip>
      </Option>);
    });

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          title={<FormattedMessage id={`resource.${type}.header`} />}
          visible={visible}
          className="c7ncd-resource-sidebar"
          footer={
            [<Button
              key="submit"
              type="primary"
              funcType="raised"
              onClick={this.handleSubmit}
              loading={submitting}
              disabled={hasEditorError}
            >
              {formatMessage({ id: type })}
            </Button>,
              <Button
                key="back"
                funcType="raised"
                onClick={this.handleClose.bind(this, false)}
                disabled={submitting}
                className="c7n-resource-footer"
              >
                {formatMessage({ id: 'cancel' })}
              </Button>]
          }
        >
          <Content
            code={`resource.${type}`}
            values={{ name: resourceName || name }}
            className="sidebar-content"
          >
            <Form layout="vertical">
              {type === 'create' && (<Fragment>
                <FormItem
                  className="c7n-select_512"
                  {...formItemLayout}
                >
                  {getFieldDecorator('envId', {
                    initialValue: envId || undefined,
                    rules: [
                      {
                        required: true,
                        message: formatMessage({ id: 'required' }),
                      },
                    ],
                  })(
                    <Select
                      className="c7n-select_512"
                      label={<FormattedMessage id="environment" />}
                      optionFilterProp="children"
                      getPopupContainer={triggerNode => triggerNode.parentNode}
                      filterOption={(input, option) => option.props.children[1]
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
                      }
                      filter
                    >
                      {envOptions}
                    </Select>,
                  )}
                </FormItem>
                <div className="c7ncd-resource-add">
                  <FormattedMessage id="resource.add" />
                </div>
                <div className="c7ncd-resource-tips">
                  <Icon type="error" className="c7ncd-resource-tips-icon" />
                  <FormattedMessage id="resource.create.tips" />
                </div>
                <div className="c7ncd-resource-mode">
                  <span>{formatMessage({ id: 'resource.mode' })}：</span>
                  <RadioGroup
                    value={mode}
                    onChange={this.changeMode}
                  >
                    {
                      _.map(['paste', 'upload'], item => (
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
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}
