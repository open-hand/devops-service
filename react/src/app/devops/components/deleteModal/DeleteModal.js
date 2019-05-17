import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Modal, Form, Input, Spin } from 'choerodon-ui';
import PropTypes from 'prop-types';
import DevopsStore from '../../stores/DevopsStore';
import EnvOverviewStore from '../../stores/project/envOverview';

import './DeleteModal.scss';

const { Item: FormItem } = Form;

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
class DeleteModal extends Component {
  static propTypes = {
    objectType: PropTypes.string.isRequired,
    visible: PropTypes.bool.isRequired,
    title: PropTypes.string.isRequired,
  };

  state = {
    deleteStatus: false,
    count: 60,
    canSendMessage: true,
    method: null,
    user: null,
    loading: true,
    isVerification: false,
    notificationId: null,
    isError: false,
    canDelete: false,
  };

  componentWillUnmount() {
    clearInterval(this.timer);
    this.timer = null;
  }

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: { projectId },
      },
      objectType,
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;

    DevopsStore.deleteCheck(projectId, envId, objectType)
      .then(data => {
        if (data && data.notificationId) {
          this.setState({
            isVerification: true,
            method: data.method,
            user: data.user,
            notificationId: data.notificationId,
          });
        }
        this.setState({ loading: false, canDelete: true });
      });
  }

  /**
   * 点击发送验证码
   */
  sendMessage = () => {
    const {
      AppState: {
        currentMenuType: { projectId },
      },
      objectId,
      objectType,
    } = this.props;

    const envId = EnvOverviewStore.getTpEnvId;

    const { notificationId, canSendMessage } = this.state;
    let count = this.state.count;

    if (this.timer || !canSendMessage) return;

    this.setState({ canSendMessage: false });

    this.timer = setInterval(() => {

      this.setState({ count: --count }, () => {
        if (count === 0) {

          clearInterval(this.timer);
          this.timer = null;

          this.setState({
            count: 60,
            canSendMessage: true,
          });

        }
      });

    }, 1000);
    DevopsStore.sendMessage(projectId, envId, objectId, notificationId, objectType);
  };

  /**
   * 判断是否可点击删除
   * @param e
   */
  checkCaptcha = (e) => {
    const value = e.target.value;
    value && value.length === 6 && this.setState({ canDelete: true });
  };

  handleDelete = (e) => {
    e.preventDefault();
    const {
      form: { validateFields },
      AppState: {
        currentMenuType: { projectId },
      },
      onOk,
      objectId,
      objectType,
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;
    this.setState({ deleteStatus: true });
    validateFields((err, data) => {
      if (!err) {
        const p = /^\d{6}$/;
        if (p.test(data.captcha)) {
          // 判断验证码是否正确
          DevopsStore.validateCaptcha(projectId, envId, objectId, data.captcha, objectType)
            .then(data => {
              if (data) {
                onOk();
              } else {
                this.setState({ isError: true });
              }
              this.setState({ deleteStatus: false });
            })
            .catch(e => {
              this.setState({ deleteStatus: false });
              Choerodon.handleResponseError(e);
            });
        } else {
          this.setState({ deleteStatus: false, isError: true });
        }
      } else {
        this.setState({ deleteStatus: false });
      }
    });
  };

  render() {
    const {
      intl: { formatMessage },
      form: { getFieldDecorator },
      visible,
      onClose,
      onOk,
      title,
      objectType,
    } = this.props;
    const {
      deleteStatus,
      count,
      canSendMessage,
      method,
      user,
      isVerification,
      loading,
      isError,
      canDelete,
    } = this.state;

    return (
      <Modal
        visible={visible}
        title={title}
        closable={false}
        footer={[
          <Button
            key="back"
            onClick={onClose}
            disabled={deleteStatus}
          >
            <FormattedMessage id="cancel" />
          </Button>,
          <Button
            key="submit"
            type="danger"
            loading={deleteStatus}
            disabled={!canDelete}
            onClick={isVerification ? this.handleDelete : onOk}
          >
            <FormattedMessage id="delete" />
          </Button>,
        ]}
      >
        <div className="c7ncd-deleteModal-wrap">
          {loading ? <Spin spinning /> : (<Fragment>
            <FormattedMessage id={`${objectType}.delete.tooltip`} />
            {isVerification && (
              <div>
                <FormattedMessage
                  id={`${objectType}.delete.verification.tooltip`}
                  values={{ method, user }}
                />
                <div className="c7ncd-delete-check">
                  <Form>
                    <FormItem
                      className="c7ncd-formItem_355"
                      {...formItemLayout}
                    >
                      {getFieldDecorator('captcha', {
                        rules: [{
                          required: true,
                          message: formatMessage({ id: 'required' }),
                        }],
                      })(
                        <Input
                          label={<FormattedMessage id="captcha" />}
                          onChange={this.checkCaptcha}
                          maxLength={6}
                        />,
                      )}
                    </FormItem>
                  </Form>
                  <Button
                    type="primary"
                    funcType="raised"
                    onClick={this.sendMessage}
                    disabled={!canSendMessage}
                  >
                    {canSendMessage ? <FormattedMessage id="send_captcha" /> :
                      <span className="c7ncd-time-span">{count}s</span>}
                  </Button>
                </div>
                {isError && <FormattedMessage id="captcha_error" />}
              </div>
            )}
          </Fragment>)}
        </div>
      </Modal>
    );
  }
}

export default DeleteModal;
