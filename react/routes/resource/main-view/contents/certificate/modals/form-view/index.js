/* eslint-disable no-underscore-dangle,react/no-access-state-in-setstate */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content } from '@choerodon/boot';
import _ from 'lodash';
import classnames from 'classnames';
import {
  Button,
  Form,
  Select,
  Input,
  Modal,
  Icon,
  Radio,
} from 'choerodon-ui';
import CertConfig from '../../../../../../../components/certConfig';
import Tips from '../../../../../../../components/Tips/Tips';
import InterceptMask from '../../../../../../../components/interceptMask/InterceptMask';
import { handlePromptError } from '../../../../../../../utils';

import '../../../../../../main.scss';
import './index.less';

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { Option } = Select;
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

const CERT_TYPE_REQUEST = 'request';
const CERT_TYPE_UPLOAD = 'upload';
const CERT_TYPE_CHOOSE = 'choose';

@Form.create({})
@injectIntl
@inject('AppState')
@observer
export default class CertificateCreate extends Component {
  state = {
    type: CERT_TYPE_REQUEST,
    submitting: false,
    suffix: null,
    certId: null,
    uploadMode: false,
  };

  domainCount = 1;

  /**
   * 域名名称唯一性校验
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      intl: { formatMessage },
    } = this.props;

    const pattern = /^([a-z0-9]([-a-z0-9]?[a-z0-9])*)$/;

    if (!pattern.test(value)) {
      callback(formatMessage({ id: 'ctf.names.check.failed' }));
    } else {
      const {
        store,
        form,
        AppState: {
          currentMenuType: {
            id: projectId,
          },
        },
        envId,
      } = this.props;

      store
        .checkCertName(projectId, value, envId)
        .then((data) => {
          if (data) {
            callback();
          } else {
            callback(formatMessage({ id: 'ctf.name.check.exist' }));
          }
        })
        .catch(() => callback());
    }
  }, 600);

  handleSubmit = (e) => {
    e.preventDefault();
    const {
      form,
      store,
      envId,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const {
      suffix,
      uploadMode,
    } = this.state;

    this.setState({ submitting: true });

    form.validateFieldsAndScroll(async (err, data) => {
      if (!err) {
        const _data = { ...data };
        const formData = new FormData();
        const excludeProps = ['domainArr', 'cert', 'key'];

        _data.domains = _.filter(_data.domains);
        _data.envId = envId;

        if (_data.type === CERT_TYPE_CHOOSE) {
          _data.type = CERT_TYPE_REQUEST;
          _data.domains = _.map(_data.domains, item => `${item}${suffix}`);
        } else if (_data.type === CERT_TYPE_UPLOAD && uploadMode) {
          const { key, cert } = data;

          formData.append('key', key.file);
          formData.append('cert', cert.file);
        }

        _.forEach(_data, (value, k) => {
          if (!_.includes(excludeProps, k)) {
            formData.append(k, value);
          }
        });

        const response = await store.createCert(projectId, formData)
          .catch((error) => {
            Choerodon.handleResponseError(error);
            this.setState({ submitting: false });
          });

        this.setState({ submitting: false });

        if (handlePromptError(response, false)) {
          this.handleClose(true);
        }
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  /**
   * 关闭弹框
   */
  handleClose = (isload = false) => {
    const { onClose } = this.props;
    onClose(isload);
  };

  /**
   * 添加域名时唯一性校验
   * @param value
   * @returns {boolean}
   */
  isUniqCheck = (value) => {
    const {
      form: { getFieldValue },
    } = this.props;
    const keyCount = _.countBy(getFieldValue('domains'));

    return keyCount[value] < 2;
  };

  /**
   * 域名格式检查
   * @param rule
   * @param value
   * @param callback
   */
  checkDomain = (rule, value, callback) => {
    const {
      intl: { formatMessage },
    } = this.props;
    const { type } = this.state;

    let pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (type === CERT_TYPE_CHOOSE) {
      pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*)$/;
    }

    if (pattern.test(value)) {
      if (this.isUniqCheck(value)) {
        callback();
      } else {
        callback(formatMessage({ id: 'ctf.domain.check.repeat' }));
      }
    } else {
      callback(formatMessage({ id: 'ctf.domain.check.failed' }));
    }
  };

  /**
   * 添加域名
   */
  addDomain = () => {
    const {
      form: {
        getFieldValue,
        setFieldsValue,
      },
    } = this.props;
    const keys = getFieldValue('domainArr');
    const uuid = this.domainCount;
    const nextKeys = _.concat(keys, uuid);
    this.domainCount = uuid + 1;
    setFieldsValue({
      domainArr: nextKeys,
    });
  };

  /**
   * 删除一个域名
   * @param k
   */
  removeGroup = (k) => {
    const {
      form: {
        getFieldValue,
        setFieldsValue,
        validateFields,
      },
    } = this.props;

    const keys = getFieldValue('domainArr');

    if (keys.length === 1) return;

    setFieldsValue({
      domainArr: _.filter(keys, key => key !== k),
    });
    setFieldsValue({
      [`domains[${k}]`]: undefined,
    });

    validateFields(['domains'], { force: true });
  };

  /**
   * 切换参数类型
   * @param e
   */
  handleTypeChange = (e) => {
    const {
      store,
      form: {
        resetFields,
        setFieldsValue,
      },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const type = e.target.value;

    if (type === CERT_TYPE_CHOOSE) {
      store.loadCert(projectId);
    }

    this.setState({
      type,
      suffix: null,
      certId: null,
    });

    resetFields(['domainArr']);
    setFieldsValue({ 'domains[0]': '' });
  };

  /**
   * 选择证书
   * @param value
   */
  handleCertSelect = (value) => {
    const { store } = this.props;
    const cert = store.getCert;
    const data = _.filter(cert, ['id', value]);
    this.setState({
      suffix: data.length ? `.${data[0].domain}` : null,
      certId: value,
    });
  };

  changeUploadMode = () => {
    this.setState({
      uploadMode: !this.state.uploadMode,
    });
  };

  get getChooseContent() {
    const {
      form: {
        getFieldDecorator,
      },
      intl: { formatMessage },
      store: {
        getCert: certs,
      },
    } = this.props;

    const certOptions = _.map(certs, ({ id, name }) => <Option key={id} value={id}>
      {name}
    </Option>);

    return (
      <div className="c7ncd-sidebar-select">
        <FormItem
          {...formItemLayout}
        >
          {getFieldDecorator('certId', {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
            ],
          })(
            <Select
              label={<FormattedMessage id="ctf.choose" />}
              placeholder={formatMessage({ id: 'ctf.choose' })}
              optionFilterProp="children"
              onChange={this.handleCertSelect}
              filterOption={(input, option) => option.props.children
                .toLowerCase()
                .indexOf(input.toLowerCase()) >= 0
              }
              filter
            >
              {certOptions}
            </Select>,
          )}
        </FormItem>
        <Tips type="form" data="ctf.choose.tips" />
      </div>
    );
  }

  /**
   * 每当域名输入改变，强制校验所有域名，消除重复域名的报错信息
   */
  changeDomainValue = _.debounce(() => {
    const {
      form: {
        validateFields,
      },
    } = this.props;

    validateFields(['domains'], { force: true });
  }, 400);

  render() {
    const {
      visible,
      form,
      intl: { formatMessage },
      store: {
        getEnvData: envs,
      },
      envId,
      AppState: {
        currentMenuType: {
          name: menuName,
        },
      },
    } = this.props;

    const {
      submitting,
      type,
      suffix,
      certId,
      uploadMode,
    } = this.state;

    const {
      getFieldDecorator,
      getFieldValue,
    } = form;

    getFieldDecorator('domainArr', { initialValue: [0] });

    const domainArr = getFieldValue('domainArr');
    const isDomainGroup = domainArr.length > 1;

    const domainItems = _.map(domainArr, k => (
      <div
        key={`domains-${k}`}
        className="creation-panel-group c7n-form-domains"
      >
        <FormItem
          className="creation-form-item"
          {...formItemLayout}
        >
          {getFieldDecorator(`domains[${k}]`, {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: this.checkDomain,
              },
            ],
          })(
            <Input
              type="text"
              maxLength={50}
              suffix={suffix}
              label={<FormattedMessage id="ctf.config.domain" />}
              disabled={type === 'choose' && !certId}
              onChange={this.changeDomainValue}
            />,
          )}
        </FormItem>
        {isDomainGroup ? (
          <Icon
            className="creation-panel-icon"
            type="delete"
            onClick={() => this.removeGroup(k)}
          />
        ) : null}
      </div>
    ));

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          cancelText={<FormattedMessage id="cancel" />}
          okText={<FormattedMessage id="create" />}
          title={<FormattedMessage id="ctf.sidebar.create" />}
          visible={visible}
          onOk={this.handleSubmit}
          onCancel={this.handleClose}
          confirmLoading={submitting}
          width={380}
        >
          <Content
            className="c7ncd-deployment-ctf-create sidebar-content"
          >
            <Form layout="vertical">
              <FormItem {...formItemLayout}>
                {getFieldDecorator('certName', {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                    {
                      validator: this.checkName,
                    },
                  ],
                })(
                  <Input
                    autoFocus
                    maxLength={40}
                    type="text"
                    label={<FormattedMessage id="ctf.name" />}
                  />,
                )}
              </FormItem>
              <div className="c7n-creation-title">
                <Icon type="settings" />
                <FormattedMessage id="ctf.config" />
              </div>
              <FormItem
                label={<FormattedMessage id="ctf.target.type" />}
                {...formItemLayout}
              >
                {getFieldDecorator('type', {
                  initialValue: CERT_TYPE_REQUEST,
                })(
                  <RadioGroup name="type" onChange={this.handleTypeChange}>
                    <Radio value={CERT_TYPE_REQUEST}>
                      <FormattedMessage id="ctf.apply" />
                    </Radio>
                    <Radio value={CERT_TYPE_UPLOAD}>
                      <FormattedMessage id="ctf.upload" />
                    </Radio>
                    <Radio value={CERT_TYPE_CHOOSE}>
                      <FormattedMessage id="ctf.choose" />
                    </Radio>
                  </RadioGroup>,
                )}
              </FormItem>
              <div className="c7n-creation-panel">
                {type === 'choose' && this.getChooseContent}
                {domainItems}
                <FormItem
                  className="creation-panel-button"
                  {...formItemLayout}
                >
                  <Button
                    type="primary"
                    funcType="flat"
                    onClick={this.addDomain}
                    icon="add"
                  >
                    <FormattedMessage id="ctf.config.add" />
                  </Button>
                </FormItem>
                {type === CERT_TYPE_UPLOAD && <Fragment>
                  <div className="ctf-upload-head">
                    <Tips
                      type="title"
                      data="certificate.file.add"
                      help={!uploadMode}
                    />
                    <Button
                      type="primary"
                      funcType="flat"
                      onClick={this.changeUploadMode}
                    >
                      <FormattedMessage id="ctf.upload.mode" />
                    </Button>
                  </div>
                  {CertConfig(uploadMode, form, formatMessage)}
                </Fragment>}
              </div>
            </Form>
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}
