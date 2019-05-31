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
import CertConfig from '../../../../components/certConfig';
import Tips from '../../../../components/Tips/Tips';
import InterceptMask from '../../../../components/interceptMask/InterceptMask';
import { HEIGHT } from '../../../../common/Constants';
import { handleCheckerProptError } from '../../../../utils';

import '../../../main.scss';
import './CertificateCreate.scss';

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
    keyLoad: false,
    certLoad: false,
    suffix: null,
    certId: null,
    uploadMode: false,
  };

  domainCount = 1;

  /**
   * 与域名相同的校验
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
      } = this.props;

      const envId = form.getFieldValue('envId');

      store
        .checkCertName(projectId, value, envId)
        .then(data => {
          if (data) {
            callback();
          } else {
            callback(formatMessage({ id: 'ctf.name.check.exist' }));
          }
        })
        .catch(() => callback());
    }
  }, 1000);

  componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    store.loadEnvData(projectId);
  }

  handleSubmit = e => {
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

    const { suffix } = this.state;

    this.setState({ submitting: true });

    form.validateFieldsAndScroll(async (err, data) => {
      if (!err) {
        const _data = { ...data };

        if (_data.type === CERT_TYPE_CHOOSE) {
          _data.type = CERT_TYPE_UPLOAD;
          _data.domains = _.map(data.domains, item => `${item}${suffix}`);
        }

        const response = await store.createCert(projectId, _data)
          .catch(error => {
            Choerodon.handleResponseError(error);
            this.setState({ submitting: false });
          });

        this.setState({ submitting: false });

        if (handleCheckerProptError(response)) {
          const initSize = HEIGHT <= 900 ? 10 : 15;

          store.initTableFilter(filter);
          store.loadCertData(true, projectId, 0, initSize,
            { field: 'id', order: 'descend' },
            { searchParam: {}, param: '' },
            envId,
          );
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
  handleClose = (isload = true) => {
    const { onClose } = this.props;
    onClose(isload);
  };

  /**
   * 域名格式检查
   * @param rule
   * @param value
   * @param callback
   */
  checkDomain = (rule, value, callback) => {
    const { intl, form } = this.props;
    const { type } = this.state;
    const { getFieldValue } = form;

    let pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (type === 'choose') {
      pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*)$/;
    }

    const keyCount = _.countBy(getFieldValue('domains'));
    if (pattern.test(value)) {
      if (keyCount[value] < 2) {
        callback();
      } else {
        callback(intl.formatMessage({ id: 'ctf.domain.check.repeat' }));
      }
    } else {
      callback(intl.formatMessage({ id: 'ctf.domain.check.failed' }));
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
  removeGroup = k => {
    const {
      form: {
        getFieldValue,
        setFieldsValue,
      },
    } = this.props;

    const keys = getFieldValue('domainArr');
    if (keys.length === 1) return;

    setFieldsValue({
      domainArr: _.filter(keys, key => key !== k),
    });
  };

  /**
   * 切换参数类型
   * @param e
   */
  handleTypeChange = e => {
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

    this.setState({ type, suffix: null, certId: null });

    resetFields(['domainArr']);
    setFieldsValue({ 'domains[0]': '' });
  };

  /**
   * 选择证书
   * @param value
   */
  handleCertSelect = value => {
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

    const certOptions = _.map(certs, ({ id, name }) =>
      <Option key={id} value={id}>
        {name}
      </Option>);

    return (
      <div className="c7ncd-sidebar-select">
        <FormItem
          className="c7n-select_480"
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
              className="c7n-select_480"
              label={<FormattedMessage id="ctf.choose" />}
              placeholder={formatMessage({ id: 'ctf.choose' })}
              optionFilterProp="children"
              onChange={this.handleCertSelect}
              filterOption={(input, option) =>
                option.props.children
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

  render() {
    const {
      visible,
      form: {
        getFieldDecorator,
        getFieldValue,
      },
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

    getFieldDecorator('domainArr', { initialValue: [0] });

    const domainArr = getFieldValue('domainArr');
    const isDomainGroup = domainArr.length > 1;
    const domainClass = classnames({
      'creation-form-item': true,
      'c7n-select_454': isDomainGroup,
      'c7n-select_480': !isDomainGroup,
    });

    const domainItems = _.map(domainArr, k => (
      <div
        key={`domains-${k}`}
        className="creation-panel-group c7n-form-domains"
      >
        <FormItem
          className={domainClass}
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
              label={<FormattedMessage id="ctf.config.domain" />}
              suffix={suffix}
              disabled={type === 'choose' && !certId}
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

    const envOptions = _.map(envs, ({ id, connect, name }) => {
      const statusClass = classnames({
        'c7ncd-status': true,
        'c7ncd-status-success': connect,
        'c7ncd-status-disconnect': !connect,
      });
      return (
        <Option key={id} value={id} disabled={!connect}>
          <span className={statusClass} />
          {name}
        </Option>
      );
    });

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          cancelText={<FormattedMessage id="cancel" />}
          okText={<FormattedMessage id="create" />}
          title={<FormattedMessage id="ctf.sidebar.create" />}
          visible={visible}
          onOk={this.handleSubmit}
          onCancel={this.handleClose.bind(this, false)}
          confirmLoading={submitting}
        >
          <Content
            code="ctf.create"
            values={{ name: menuName }}
            className="c7n-ctf-create sidebar-content"
          >
            <Form layout="vertical" className="c7n-sidebar-form">
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator('envId', {
                  initialValue: envs.length ? envId : undefined,
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                  ],
                })(
                  <Select
                    className="c7n-select_512"
                    label={<FormattedMessage id="ctf.envName" />}
                    placeholder={formatMessage({ id: 'ctf.env.placeholder' })}
                    optionFilterProp="children"
                    onSelect={this.handleEnvSelect}
                    getPopupContainer={triggerNode => triggerNode.parentNode}
                    filterOption={(input, option) =>
                      option.props.children[1]
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
                    }
                    filter
                    showSearch
                  >
                    {envOptions}
                  </Select>,
                )}
              </FormItem>
              <FormItem className="c7n-select_512" {...formItemLayout}>
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
                    disabled={!getFieldValue('envId')}
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
              <div className="c7n-creation-radio">
                <div className="creation-radio-label">
                  <FormattedMessage id="chooseType" />
                </div>
                <FormItem
                  className="c7n-select_512 creation-radio-form"
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
              </div>
              <div className="c7n-creation-panel">
                {type === 'choose' && this.getChooseContent}
                {domainItems}
                <FormItem
                  className="c7n-select_480 creation-panel-button"
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
                    <Tips type="title" data="certificate.file.add" />
                    <Button
                      type="primary"
                      funcType="flat"
                      onClick={this.changeUploadMode}
                    >
                      <FormattedMessage id="ctf.upload.mode" />
                    </Button>
                  </div>
                  <CertConfig
                    isUploadMode={uploadMode}
                  />
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
