import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Modal, Form, Input, Spin } from 'choerodon-ui';
import PropTypes from 'prop-types';
import DevopsStore from '../../stores/DevopsStore';
import EnvOverviewStore from '../../stores/project/envOverview';
import { handleProptError } from '../../utils';

import './DeleteModal.scss';

const { Item: FormItem } = Form;

@Form.create({})
@injectIntl
@inject('AppState')
@observer
class DeleteModal extends Component {
  static propTypes = {
    objectType: PropTypes.string.isRequired,
    visible: PropTypes.bool.isRequired,
    title: PropTypes.string.isRequired,
    objectId: PropTypes.number,
    loading: PropTypes.bool,
  };

  static defaultProps = {
    loading: false,
  };

  constructor(props) {
    super(props);
    this.state = {
      ...this.getInitState,
    };
  }

  get getInitState() {
    return {
      count: 60,
      checkLoading: true,
      validateLoading: false,
      isError: false,
      canDelete: false,
      canSendMessage: true,
      isVerification: false,
      user: null,
      method: null,
      notificationId: null,
    };
  }

  componentDidMount() {
    this.initCheck();
  }

  componentDidUpdate(prevProps, prevState) {
    // NOTE: 由于删除模态框在未确认删除前不会销毁
    //       所以每次显示模态框时
    //       都要校验下是否需要发验证码进行二次校验
    if (this.props.visible && prevProps.visible !== this.props.visible) {
      this.initCheck();
    }
  }

  clearTimer() {
    clearInterval(this.timer);
    this.timer = null;
  }

  async initCheck() {
    const {
      AppState: {
        currentMenuType: { id: projectId },
      },
      objectType,
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;

    this.setState({ checkLoading: true });
    const response = await DevopsStore.deleteCheck(projectId, envId, objectType)
      .catch(() => {
        this.setState({ checkLoading: false });
      });

    const result = handleProptError(response);

    if (result && result.notificationId) {
      this.setState({
        isVerification: true,
        method: result.method,
        user: result.user,
        notificationId: result.notificationId,
        canDelete: false,
      });
    } else {
      this.clearTimer();
      this.setState({
        ...this.getInitState,
        canDelete: true,
      });
    }
    this.setState({ checkLoading: false });
  }

  /**
   * 点击发送验证码
   */
  sendMessage = async () => {
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

    this.setState({ canSendMessage: false, isError: false });

    await DevopsStore.sendMessage(projectId, envId, objectId, notificationId, objectType)
      .catch(e => {
        Choerodon.handleResponseError(e);
      });

    // NOTE: 无论验证码是否发送成功，都要经过1分钟后重新发送
    this.timer = setInterval(() => {

      this.setState({ count: --count }, () => {
        if (count === 0) {

          this.clearTimer();

          this.setState({
            count: 60,
            canSendMessage: true,
          });

        }
      });

    }, 1000);
  };

  /**
   * 判断是否可点击删除
   * @param e
   */
  checkCaptcha = (e) => {
    const value = e.target.value;
    this.setState({
      canDelete: value && value.length === 6,
      isError: false,
    });
  };

  handleDelete = (e) => {
    e.preventDefault();

    const {
      form: { validateFields },
      AppState: {
        currentMenuType: { id: projectId },
      },
      onOk,
      objectId,
      objectType,
    } = this.props;

    const { isVerification } = this.state;

    if (!isVerification) {
      onOk(objectId);
      return;
    }

    const envId = EnvOverviewStore.getTpEnvId;

    this.setState({ validateLoading: true });

    validateFields((err, { captcha }) => {
      if (!err) {

        DevopsStore.validateCaptcha(projectId, envId, objectId, captcha, objectType)
          .then(data => {
            if (data && data.failed) {
              this.setState({ isError: true, canDelete: false });
            } else {
              this.clearTimer();
              onOk(objectId, () => {
                this.clearTimer();
                this.setState({
                  count: 60,
                  canSendMessage: true,
                  canDelete: false,
                });
              });
            }
            this.setState({ validateLoading: false });
          })
          .catch(e => {
            this.setState({ validateLoading: false });
            Choerodon.handleResponseError(e);
          });

      } else {
        this.setState({ validateLoading: false });
      }
    });
  };

  closeModal = () => {
    const {
      form: { resetFields },
      onClose,
      objectId,
    } = this.props;
    onClose(objectId);
    resetFields(['captcha']);
    this.setState({ isError: false });
  };

  get getContent() {
    const {
      intl: { formatMessage },
      form: { getFieldDecorator },
      objectType,
    } = this.props;

    const {
      count,
      canSendMessage,
      method,
      user,
      isVerification,
      checkLoading,
      isError,
    } = this.state;

    let content = <Spin spinning />;

    if (!checkLoading) {
      content = isVerification ? (
        <Fragment>
          <FormattedMessage id={`${objectType}.delete.verify.message`} />
          <br />
          <FormattedMessage
            id="delete.verify.message"
            values={{ method, user }}
          />
          <div className="c7ncd-delete-check">
            <Form className="c7ncd-captcha-form">
              <FormItem>
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
              className="c7ncd-captcha-btn"
              type="primary"
              funcType="raised"
              onClick={this.sendMessage}
              disabled={!canSendMessage}
            >
              {
                canSendMessage
                  ? <FormattedMessage id="captcha.send" />
                  : <div><span>{count}</span> <FormattedMessage id="captcha.resend" /></div>
              }
            </Button>
          </div>
          {isError && <span className="c7ncd-captcha-error">{formatMessage({ id: 'captcha.error' })}</span>}
        </Fragment>
      ) : <FormattedMessage id={`${objectType}.delete.message`} />;
    }

    return content;
  }

  render() {
    const {
      visible,
      title,
      loading: deleteLoading,
    } = this.props;
    const {
      validateLoading,
      canDelete,
      isVerification,
    } = this.state;

    return (
      <Modal
        visible={visible}
        title={title}
        closable={false}
        footer={[
          <Button
            key="back"
            onClick={this.closeModal}
            disabled={validateLoading || deleteLoading}
          >
            <FormattedMessage id={isVerification ? 'close' : 'cancel'} />
          </Button>,
          <Button
            key="submit"
            type="danger"
            loading={validateLoading || deleteLoading}
            disabled={!canDelete}
            onClick={this.handleDelete}
          >
            <FormattedMessage id="delete" />
          </Button>,
        ]}
      >
        <div className="c7ncd-deleteModal-wrap">
          {this.getContent}
        </div>
      </Modal>
    );
  }
}

export default DeleteModal;
