import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { stores, Content } from '@choerodon/boot';
import _ from 'lodash';
import {
  Button,
  Form,
  Select,
  Input,
  Modal,
  Icon,
  Radio,
} from 'choerodon-ui';
import '../../../main.scss';
import './CertificateCreate.scss';
import Tips from '../../../../components/Tips/Tips';
import InterceptMask from '../../../../components/interceptMask/InterceptMask';

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

const { AppState } = stores;
const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { Option } = Select;
const { Group: RadioGroup } = Radio;
const { TextArea } = Input;
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

@observer
class CertificateCreate extends Component {
  /**
   * 与域名相同的校验
   */
  checkName = _.debounce((rule, value, callback) => {
    const p = /^([a-z0-9]([-a-z0-9]?[a-z0-9])*)$/;
    const { intl } = this.props;
    if (p.test(value)) {
      const { store, form } = this.props;
      const { id: projectId } = AppState.currentMenuType;
      const envId = form.getFieldValue('envId');
      store
        .checkCertName(projectId, value, envId)
        .then(data => {
          if (data) {
            callback();
          } else {
            callback(intl.formatMessage({ id: 'ctf.name.check.exist' }));
          }
        })
        .catch(() => callback());
    } else {
      callback(intl.formatMessage({ id: 'ctf.names.check.failed' }));
    }
  }, 1000);

  constructor(props) {
    super(props);
    this.state = {
      submitting: false,
      type: 'request',
      keyLoad: false,
      certLoad: false,
      suffix: null,
      certId: null,
    };
    this.domainCount = 1;
  }

  componentDidMount() {
    const { store } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    store.loadEnvData(projectId);
  }

  handleSubmit = e => {
    e.preventDefault();
    const { form, store } = this.props;
    const { suffix } = this.state;
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ submitting: true });
    form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        if (data.type === 'choose') {
          data.type = 'upload';
          let list = [];
          _.map(data.domains, item => {
            list.push(`${item}${suffix}`);
          });
          data.domains = list;
        }
        const p = store.createCert(projectId, data);
        this.handleResponse(p);
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  /**
   * 处理创建证书请求所返回的数据
   * @param promise
   */
  handleResponse = promise => {
    const { store, envId } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    promise
      .then(res => {
        this.setState({ submitting: false });
        if (res && res.failed) {
          Choerodon.prompt(res.message);
        } else {
          const initSize = HEIGHT <= 900 ? 10 : 15;
          const filter = {
            page: 0,
            pageSize: initSize,
            postData: { searchParam: {}, param: '' },
            sorter: {
              field: 'id',
              columnKey: 'id',
              order: 'descend',
            },
            param: [],
            createDisplay: false,
          };
          store.setTableFilter(filter);
          store.loadCertData(
            true,
            projectId,
            0,
            initSize,
            { field: 'id', order: 'descend' },
            { searchParam: {}, param: '' },
            envId,
          );
          this.handleClose(true);
        }
      })
      .catch(error => {
        Choerodon.handleResponseError(error);
        this.setState({ submitting: false });
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
    let p = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (type === 'choose') {
      p = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*)$/;
    }
    const keyCount = _.countBy(getFieldValue('domains'));
    if (p.test(value)) {
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
    const { getFieldValue, setFieldsValue } = this.props.form;
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
    const { getFieldValue, setFieldsValue } = this.props.form;
    const keys = getFieldValue('domainArr');
    if (keys.length === 1) {
      return;
    }
    setFieldsValue({
      domainArr: _.filter(keys, key => key !== k),
    });
  };

  /**
   * 获取环境选择器的元素节点
   * @param node
   */
  envSelectRef = node => {
    if (node) {
      this.envSelect = node.rcSelect;
    }
  };

  /**
   * 切换参数类型
   * @param e
   */
  handleTypeChange = e => {
    const { store, form } = this.props;
    const { resetFields, setFieldsValue } = form;
    const { projectId } = AppState.currentMenuType;
    const type = e.target.value;
    if (type === 'choose') {
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
      suffix: data && data.length ? `.${data[0].domain}` : null,
      certId: value,
    });
  };

  render() {
    const { visible, form, intl, store, envId } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    const { submitting, type, suffix, certId } = this.state;
    const { name: menuName, id: projectId } = AppState.currentMenuType;
    getFieldDecorator('domainArr', { initialValue: [0] });
    const domainArr = getFieldValue('domainArr');
    const domainItems = _.map(domainArr, (k, index) => (
      <div key={`domains-${k}`} className="creation-panel-group c7n-form-domains">
        <FormItem
          className={`c7n-select_${
            domainArr.length > 1 ? 454 : 480
            } creation-form-item`}
          {...formItemLayout}
        >
          {getFieldDecorator(`domains[${k}]`, {
            rules: [
              {
                required: true,
                message: intl.formatMessage({ id: 'required' }),
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
        {domainArr.length > 1 ? (
          <Icon
            className="creation-panel-icon"
            type="delete"
            onClick={() => this.removeGroup(k)}
          />
        ) : null}
      </div>
    ));
    const env = store.getEnvData;
    const cert = store.getCert;
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
              <FormItem className="c7n-select_512" {...formItemLayout}>
                {getFieldDecorator('envId', {
                  initialValue: env.length ? envId : undefined,
                  rules: [
                    {
                      required: true,
                      message: intl.formatMessage({ id: 'required' }),
                    },
                  ],
                })(
                  <Select
                    ref={this.envSelectRef}
                    className="c7n-select_512"
                    label={<FormattedMessage id="ctf.envName" />}
                    placeholder={intl.formatMessage({
                      id: 'ctf.env.placeholder',
                    })}
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
                    {_.map(env, item => {
                      const { id, connect, name } = item;
                      return (
                        <Option key={id} value={id} disabled={!connect}>
                          {connect ? (
                            <span className="c7ncd-status c7ncd-status-success" />
                          ) : (
                            <span className="c7ncd-status c7ncd-status-disconnect" />
                          )}
                          {name}
                        </Option>
                      );
                    })}
                  </Select>,
                )}
              </FormItem>
              <FormItem className="c7n-select_512" {...formItemLayout}>
                {getFieldDecorator('certName', {
                  rules: [
                    {
                      required: true,
                      message: intl.formatMessage({ id: 'required' }),
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
                    initialValue: 'request',
                  })(
                    <RadioGroup name="type" onChange={this.handleTypeChange}>
                      <Radio value="request">
                        <FormattedMessage id="ctf.apply" />
                      </Radio>
                      <Radio value="upload">
                        <FormattedMessage id="ctf.upload" />
                      </Radio>
                      <Radio value="choose">
                        <FormattedMessage id="ctf.choose" />
                      </Radio>
                    </RadioGroup>,
                  )}
                </FormItem>
              </div>
              <div className="c7n-creation-panel">
                {type === 'choose' && <div className="c7ncd-sidebar-select">
                  <FormItem
                    className="c7n-select_480"
                    {...formItemLayout}
                  >
                    {getFieldDecorator('certId', {
                      rules: [
                        {
                          required: true,
                          message: intl.formatMessage({ id: 'required' }),
                        },
                      ],
                    })(
                      <Select
                        className="c7n-select_480"
                        label={<FormattedMessage id="ctf.choose" />}
                        placeholder={intl.formatMessage({
                          id: 'ctf.choose',
                        })}
                        optionFilterProp="children"
                        onChange={this.handleCertSelect}
                        filterOption={(input, option) =>
                          option.props.children
                            .toLowerCase()
                            .indexOf(input.toLowerCase()) >= 0
                        }
                        filter
                      >
                        {_.map(cert, item => {
                          const { id, name } = item;
                          return (
                            <Option key={id} value={id}>
                              {name}
                            </Option>
                          );
                        })}
                      </Select>,
                    )}
                  </FormItem>
                  <Tips type="form" data="ctf.choose.tips" />
                </div>}
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
                {type === 'upload' ? (
                  <Fragment>
                    <div className="ctf-upload-head">
                      <Tips type="title" data="certificate.file.add" />
                    </div>
                    <FormItem
                      className="c7n-select_480"
                      {...formItemLayout}
                      label={<FormattedMessage id="certificate.cert.content" />}
                    >
                      {getFieldDecorator('certValue', {
                        rules: [
                          {
                            required: true,
                            message: intl.formatMessage({ id: 'required' }),
                          },
                        ],
                      })(
                        <TextArea
                          autosize={{
                            minRows: 2,
                          }}
                          label={<FormattedMessage id="certificate.cert.content" />}
                        />,
                      )}
                    </FormItem>
                    <FormItem
                      className="c7n-select_480"
                      {...formItemLayout}
                      label={<FormattedMessage id="certificate.key.content" />}
                    >
                      {getFieldDecorator('keyValue', {
                        rules: [
                          {
                            required: true,
                            message: intl.formatMessage({ id: 'required' }),
                          },
                        ],
                      })(
                        <TextArea
                          autosize={{
                            minRows: 2,
                          }}
                          label={<FormattedMessage id="certificate.key.content" />}
                        />,
                      )}
                    </FormItem>
                  </Fragment>
                ) : null}
              </div>
            </Form>
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}

export default Form.create({})(injectIntl(CertificateCreate));
