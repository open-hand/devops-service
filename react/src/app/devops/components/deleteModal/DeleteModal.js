import React, { Component, Fragment } from "react";
import { observer, inject } from "mobx-react";
import { injectIntl, FormattedMessage } from "react-intl";
import { Button, Modal, Form, Input, Spin } from "choerodon-ui";
import PropTypes from "prop-types";
import _ from 'lodash';
import DevopsStore from '../../stores/DevopsStore';
import EnvOverviewStore from "../../stores/project/envOverview";

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

  constructor(props) {
    super(props);
    this.state = {
      deleteStatus: false,
      count: 60,
      cansendMessage: true,
      method: null,
      user: null,
      loading: true,
      isVerification: false,
      notificationId: null,
      isError: false,
      canDelete: false,
    };
  }

  componentDidMount () {
    const {
      AppState: {
        currentMenuType: { projectId },
      },
      objectType,
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;
    // 发送请求判断是否需要验证
    // DevopsStore.deleteCheck(projectId, envId, objectType)
    //   .then(data => {
    //     if (data && data.notificationId) {
    //       this.setState({
    //         isVerification: true,
    //         method: data.method,
    //         user: data.user,
    //         notificationId: data.notificationId,
    //       });
    //     }
    //     this.setState({ loading: false })
    //   });
    this.setState({
      isVerification: true,
      method: "短信",
      user: "20399林岩芳",
      notificationId: 1,
      loading: false,
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
    const { notificationId } = this.state;
    let count = this.state.count;
    const timer = setInterval(() => {
      this.setState({ count: (count--), cansendMessage: false }, () => {
        if (count === 0) {
          clearInterval(timer);
          this.setState({
            count: 60,
            cansendMessage: true,
          })
        }
      });
    }, 1000);
    // 点击发送验证码
    // DevopsStore.sendMessage(projectId, envId, objectId, notificationId, objectType);
  };

  checkCaptcha = (e) => {
    const value = e.target.value;
    value && value.length === 6 && this.setState({ canDelete: true})
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
                onOk()
              } else {
                this.setState({isError: true})
              }
              this.setState({deleteStatus: false});
            })
            .catch(e => {
              this.setState({deleteStatus: false});
              Choerodon.handleResponseError(e);
            })
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
      cansendMessage,
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
            disabled={canDelete}
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
                        />,
                      )}
                    </FormItem>
                  </Form>
                  <Button
                    type="primary"
                    funcType="raised"
                    onClick={this.sendMessage}
                    disabled={!cansendMessage}
                  >
                    {cansendMessage ? <FormattedMessage id="send_captcha" /> : <span className="c7ncd-time-span">{count}s</span>}
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
