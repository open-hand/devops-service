/**
 * @author ale0720@163.com
 * @date 2019-05-13 16:47
 */
import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content } from '@choerodon/master';
import _ from 'lodash';
import {
  Form,
  Select,
  Modal,
  Radio,
  Checkbox,
  Tooltip,
} from 'choerodon-ui';
import InterceptMask from '../../../../../../../../components/interceptMask/InterceptMask';
import {
  EVENT,
  TARGET_OPTIONS,
  METHOD_OPTIONS,
  TARGET_SPECIFIER,
} from '../Constants';

import '../../../../../../../main.less';
import './NotificationSidebar.less';

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { Option } = Select;
const { Group: RadioGroup } = Radio;
const { Group: CheckboxGroup } = Checkbox;
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
export default class NotificationSidebar extends Component {
  state = {
    submitting: false,
    target: null,
  };

  componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: { projectId },
      },
      type,
      id,
      envId,
    } = this.props;
    store.loadUsers(projectId);
    store.eventCheck(projectId, envId);
    if (type === 'edit' && id) {
      store.loadSingleData(projectId, id)
        .then((data) => {
          if (data && !data.failed) {
            const { notifyObject } = data;
            this.setState({
              target: notifyObject,
            });
          }
        });
    }
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setSingleData({});
    store.setDisabledEvent([]);
    store.setUsers([]);
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const {
      form,
      store,
      type,
      AppState: { currentMenuType: { projectId } },
      envId,
    } = this.props;
    this.setState({ submitting: true });
    form.validateFields((err, data) => {
      if (!err) {
        let promise = null;
        data.envId = envId;
        data.projectId = projectId;
        if (type === 'edit') {
          const { getSingleData: { id, objectVersionNumber } } = store;
          data.id = id;
          data.objectVersionNumber = objectVersionNumber;
          promise = store.updateData(projectId, data);
        } else {
          promise = store.createData(projectId, data);
        }
        this.handleResponse(promise);
      } else {
        this.setState({ submitting: false });
      }
    });
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
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          } else {
            this.handleClose(null, true);
          }
        })
        .catch((err) => {
          this.setState({ submitting: false });
          Choerodon.handleResponseError(err);
        });
    }
  };

  /**
   * 关闭弹框
   */
  handleClose = (e, reload = false) => {
    const { onClose } = this.props;
    onClose(reload);
  };


  /**
   * 修改通知对象
   */
  changeTarget = (e) => {
    this.setState({ target: e.target.value });
  };

  render() {
    const {
      visible,
      type,
      form: { getFieldDecorator },
      intl: { formatMessage },
      store,
      envId,
    } = this.props;
    const {
      submitting,
      target,
    } = this.state;
    const {
      getUsers,
      getSingleData: {
        notifyTriggerEvent,
        notifyType,
        notifyObject,
        userRelIds,
      },
      getDisabledEvent,
    } = store;

    return (
      <Sidebar
        destroyOnClose
        cancelText={<FormattedMessage id="cancel" />}
        okText={<FormattedMessage id={type} />}
        title={<FormattedMessage id={`notification.sidebar.${type}`} />}
        visible={visible}
        onOk={this.handleSubmit}
        onCancel={this.handleClose}
        confirmLoading={submitting}
        className="c7n-notifications-sidebar"
        width={400}
      >
        <Content
          code={`notification.${type}`}
          className="sidebar-content"
        >
          <Form layout="vertical">
            <FormItem {...formItemLayout}>
              {getFieldDecorator('notifyTriggerEvent', {
                initialValue: notifyTriggerEvent ? notifyTriggerEvent.slice() : undefined,
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
              })(
                <Select
                  mode="tags"
                  label={formatMessage({ id: 'notification.event' })}
                  allowClear
                  disabled={!envId}
                >
                  {
                      _.map(EVENT, (item) => {
                        const isDisabled = _.includes(getDisabledEvent, item) && !_.includes(notifyTriggerEvent, item);
                        return (
                          <Option
                            key={item}
                            value={item}
                            disabled={isDisabled}
                          >
                            <Tooltip
                              title={isDisabled ? formatMessage({ id: 'notification.event.tips' }) : ''}
                            >
                              {formatMessage({ id: `notification.event.${item}` })}
                            </Tooltip>
                          </Option>
                        );
                      })
                    }
                </Select>
              )}
            </FormItem>
            <FormItem {...formItemLayout}>
              {getFieldDecorator('notifyType', {
                initialValue: notifyType ? notifyType.slice() : undefined,
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
              })(
                <CheckboxGroup
                  label={formatMessage({ id: 'notification.method' })}
                  className="c7n-form-checkbox"
                >
                  {
                      _.map(METHOD_OPTIONS, (item) => (
                        <Checkbox
                          key={item}
                          value={item}
                          className="c7n-checkbox-item"
                        >
                          {formatMessage({ id: `notification.method.${item}` })}
                        </Checkbox>
                      ))
                    }
                </CheckboxGroup>
              )}
            </FormItem>
            <FormItem {...formItemLayout}>
              {getFieldDecorator('notifyObject', {
                initialValue: notifyObject || undefined,
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
              })(
                <RadioGroup
                  label={formatMessage({ id: 'notification.target' })}
                  onChange={this.changeTarget}
                  className="c7n-form-checkbox"
                >
                  {
                      _.map(TARGET_OPTIONS, (item) => (
                        <Radio
                          key={item}
                          value={item}
                          className="c7n-checkbox-item"
                        >
                          {formatMessage({ id: `notification.target.${item}` })}
                        </Radio>
                      ))
                    }
                </RadioGroup>
              )}
            </FormItem>
            {target === TARGET_SPECIFIER && <FormItem {...formItemLayout}>
                {getFieldDecorator('userRelIds', {
                  initialValue: userRelIds ? userRelIds.slice() : undefined,
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                  ],
                })(
                  <Select
                    mode="multiple"
                    label={formatMessage({ id: 'notification.target.specifier' })}
                    className="c7n-notifications-select-userRelIds"
                    optionFilterProp="children"
                    allowClear
                    filter
                    filterOption={(input, option) => option.props.children
                      .toLowerCase()
                      .indexOf(input.toLowerCase()) >= 0}
                  >
                    {
                      _.map(getUsers, ({ id, loginName, realName }) => (
                        <Option
                          key={id}
                          value={id}
                        >
                          {`${loginName} ${realName}`}
                        </Option>
                      ))
                    }
                  </Select>
                )}
              </FormItem>}
          </Form>
          <InterceptMask visible={submitting} />
        </Content>
      </Sidebar>
    );
  }
}
