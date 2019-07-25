import React, { Component } from 'react/index';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Input, Form, Modal } from 'choerodon-ui';
import { stores } from '@choerodon/boot';
import './index.scss';
import _ from 'lodash';

const FormItem = Form.Item;
const { AppState } = stores;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 8 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};

@observer
class EnvGroup extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  /**
   * 环境组唯一性校验
   * @type {Function}
   */
  checkGroup = _.debounce((rule, value, callback) => {
    const { store, intl } = this.props;
    const projectId = AppState.currentMenuType.id;
    const groupOne = store.getGroupOne;
    const flag = groupOne ? value !== groupOne.name : value;
    if (flag) {
      store.checkEnvGroup(projectId, value)
        .then((error) => {
          if (!error) {
            callback(intl.formatMessage({ id: 'envPl.name.check.exist' }));
          } else {
            callback();
          }
        });
    } else {
      callback();
    }
  }, 1000);

  /**
   * 创建/编辑组
   * @param e
   */
  groupOk = (e) => {
    e.preventDefault();
    const { store } = this.props;
    store.setBtnLoading(true);
    const projectId = AppState.currentMenuType.id;
    const sideType = store.getSideType;
    const groupOne = store.getGroupOne;
    if (sideType === 'createGroup') {
      this.props.form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          store.createGroup(projectId, data.group).then((res) => {
            if (res) {
              if (res && res.failed) {
                store.setBtnLoading(false);
                Choerodon.prompt(res.message);
              } else {
                store.loadGroup(projectId);
                store.setBtnLoading(false);
                store.setShowGroup(false);
                store.loadEnv(projectId, true);
                store.loadEnv(projectId, false);
                this.props.form.resetFields();
              }
            }
          }).catch(e => {
            store.setBtnLoading(false);
            Choerodon.handleResponseError(e);
          });
        }
      });
    } else {
      this.props.form.validateFieldsAndScroll((err, data, modify) => {
        if (modify) {
          if (!err) {
            const groupData = {
              id: groupOne.id,
              name: data.group,
            };
            store.updateGroup(projectId, groupData).then((res) => {
              if (res) {
                if (res && res.failed) {
                  store.setBtnLoading(false);
                  Choerodon.prompt(res.message);
                } else {
                  store.loadGroup(projectId);
                  store.setShowGroup(false);
                  store.setBtnLoading(false);
                  store.setGroupOne([]);
                  store.loadEnv(projectId, true);
                  store.loadEnv(projectId, false);
                  this.props.form.resetFields();
                }
              }
            }).catch(e => {
              store.setBtnLoading(false);
              Choerodon.handleResponseError(e);
            });
          }
        } else {
          store.setShowGroup(false);
          store.setBtnLoading(false);
        }
      });
    }
  };


  groupCancel = () => {
    const { store } = this.props;
    store.setShowGroup(false);
    store.setGroupOne([]);
    this.props.form.resetFields();
  };

  render() {
    const { store, intl, showTitle, okText } = this.props;
    const { getFieldDecorator } = this.props.form;

    const groupOne = store.getGroupOne;
    const showGroup = store.getShowGroup;
    const sideType = store.getSideType;
    const btnLoading = store.getBtnLoading;

    return (
      <Modal
        visible={showGroup}
        className="c7n-env-group-modal"
        onOk={this.groupOk}
        closable={false}
        onCancel={this.groupCancel}
        footer={[
          <Button key="back" onClick={this.groupCancel} disabled={btnLoading}><FormattedMessage id="cancel" /></Button>,
          <Button key="submit" type="primary" loading={btnLoading} onClick={this.groupOk}>
            {okText(sideType)}
          </Button>,
        ]}
      >
        <div className="group-title">{showTitle(sideType)}</div>
        <Form>
          <FormItem
            {...formItemLayout}
          >
            {getFieldDecorator('group', {
              rules: [{
                required: true,
                message: intl.formatMessage({ id: 'required' }),
              }, {
                validator: this.checkGroup,
              }],
              initialValue: groupOne ? groupOne.name : '',
            })(
              <Input label={<FormattedMessage id="envPl.form.group" />} maxLength={20} />,
            )}
          </FormItem>
        </Form>
      </Modal>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(EnvGroup)));
