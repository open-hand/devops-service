/**
 * @author ale0720@163.com
 * @date 2019-06-18 20:30
 */
import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Modal,
  Form,
  Input,
  Select,
} from 'choerodon-ui';
import { Content } from '@choerodon/boot';
import YamlEditor from '../../../../../components/yamlEditor/YamlEditor';
import InterceptMask from '../../../../../components/interceptMask/InterceptMask';
import { handlePromptError } from '../../../../../utils';

import '../../../../main.scss';
import './ConfigSidebar.scss';

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { TextArea } = Input;
const { Option } = Select;
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
export default class ConfigSidebar extends Component {
  state = {
    submitting: false,
  };

  /**
   * 创建编辑部署配置
   */
  handleSubmit = (e) => {
    e.preventDefault();
    const {
      store,
      form: {
        validateFields,
      },
      AppState: { currentMenuType: { projectId } },
      onOk,
      value,
    } = this.props;

    this.setState({ submitting: true });

    validateFields(async (err, data) => {
      if (!err) {
        const _data = {
          ...data,
          value,
        };
        const response = await store.changeConfig(projectId, _data)
          .catch(error => {
            Choerodon.handleResponseError(error);
          });
        this.setState({ submitting: false });

        if (handlePromptError(response)) {
          onOk(response.id);
        }
      }
    });
  };

  render() {
    const {
      intl: { formatMessage },
      form: {
        getFieldDecorator,
      },
      visible,
      app,
      env,
      value,
      onCancel,
    } = this.props;
    const {
      submitting,
    } = this.state;

    const appId = app ? app.appId : undefined;
    const envId = env ? env.id : undefined;

    return (
      <Sidebar
        title={formatMessage({ id: 'deploymentConfig.create.header' })}
        okText={<FormattedMessage id="create" />}
        visible={visible}
        onOk={this.handleSubmit}
        onCancel={onCancel}
        confirmLoading={submitting}
      >
        <Content
          code="deploymentConfig.create"
          className="sidebar-content"
        >
          <Form layout="vertical">
            <FormItem
              className="c7n-select_512"
              {...formItemLayout}
            >
              {getFieldDecorator('name', {
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: formatMessage({ id: 'required' }),
                  },
                  {
                    validator: this.checkName,
                  },
                ],
              })(
                <Input
                  autoFocus
                  type="text"
                  maxLength={30}
                  label={<FormattedMessage id="deploymentConfig.name" />}
                />,
              )}
            </FormItem>
            <FormItem
              className="c7n-select_512"
              {...formItemLayout}
            >
              {getFieldDecorator('description', {
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                    whitespace: true,
                  },
                ],
              })(
                <TextArea
                  label={<FormattedMessage id="deploymentConfig.des" />}
                  autosize={{ minRows: 2, maxRows: 5 }}
                />,
              )}
            </FormItem>
            <FormItem
              className="c7n-select_512"
              {...formItemLayout}
            >
              {getFieldDecorator('appId', {
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
                initialValue: appId,
              })(
                <Select
                  disabled
                  label={formatMessage({ id: 'deploy.appName' })}
                >
                  <Option
                    key={appId}
                    value={appId}
                  >
                    {app ? app.name : ''}
                  </Option>
                </Select>,
              )}
            </FormItem>
            <FormItem
              className="c7n-select_512"
              {...formItemLayout}
            >
              {getFieldDecorator('envId', {
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
                initialValue: env ? env.id : undefined,
              })(
                <Select
                  disabled
                  label={formatMessage({ id: 'deploy.envName' })}
                >
                  <Option
                    key={envId}
                    value={envId}
                  >
                    {env ? env.name : ''}
                  </Option>
                </Select>,
              )}
            </FormItem>
            <div className="c7n-deploymentConfig-value">
              <FormattedMessage id="deploy.step.config" />
            </div>
            <YamlEditor readOnly value={value || ''} />
          </Form>
          <InterceptMask visible={submitting} />
        </Content>
      </Sidebar>
    );
  }
}
