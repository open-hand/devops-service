import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Tooltip, Radio, Input, Form, Select, Icon } from 'choerodon-ui';
import _ from 'lodash';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import './index.less';

const { Item: FormItem } = Form;
const { Group: RadioGroup } = Radio;
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

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Index extends Component {
  constructor(props) {
    super(props);
    this.state = {
      protocol: 'normal',
      pathCountChange: false,
    };
    this.pathKeys = 1;
  }

  /**
   * 添加或删除包含路径的表单项触发路径的校验
   * 确保 新添加 的一组路径已经渲染在页面上
   */
  componentDidUpdate(prevProps) {
    if (this.state.pathCountChange) {
      this.triggerPathCheck();
    }
  }

  /**
   * 检查名称的唯一性
   * @type {Function}
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      intl: { formatMessage },
      AppState: { currentMenuType: { projectId } },
      DomainStore,
      envId,
    } = this.props;
    const p = /^([a-z0-9]([-a-z0-9]?[a-z0-9])*)$/;
    if (value && p.test(value)) {
      if (envId) {
        DomainStore
          .checkName(projectId, value, envId)
          .then((data) => {
            if (data) {
              callback();
            } else {
              callback(formatMessage({ id: 'domain.name.check.exist' }));
            }
          })
          .catch(() => callback());
      } else {
        callback(formatMessage({ id: 'network.form.app.disable' }));
      }
    } else {
      callback(formatMessage({ id: 'domain.name.check.failed' }));
    }
  }, 1000);

  /**
   * 检查路径
   * 和域名组合检查
   * @param rule
   * @param value
   * @param callback
   */
  checkPath = (rule, value, callback) => {
    const {
      form: { getFieldValue, getFieldError },
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      type,
      DomainStore,
      envId,
    } = this.props;
    if (value) {
      const p = /^\/(\S)*$/;
      const count = _.countBy(getFieldValue('path'));
      const domain = getFieldValue('domain');
      const domainError = getFieldError('domain');
      if (p.test(value)) {
        // 重复检查
        if (count[value] < 2) {
          // 如果域名校验不通过，则不发起域名路径组合校验
          if (!domainError) {
            const checkPromise = DomainStore.checkPath(
              projectId,
              domain,
              envId,
              encodeURIComponent(value),
            );
            this.handleCheckResponse(checkPromise, callback);
          } else {
            callback();
          }
        } else {
          callback(formatMessage({ id: 'domain.path.check.exist' }));
        }
      } else {
        callback(formatMessage({ id: 'domain.path.check.failed' }));
      }
    } else {
      callback(formatMessage({ id: 'domain.path.check.notSet' }));
    }
  };

  /**
   * 处理路径校验返回结果
   * @param promise
   * @param callback
   */
  handleCheckResponse = (promise, callback) => {
    const { intl: { formatMessage } } = this.props;
    if (promise) {
      promise
        .then((data) => {
          if (data) {
            callback();
          } else {
            callback(formatMessage({ id: 'domain.path.check.exist' }));
          }
        })
        .catch((err) => {
          Choerodon.handleResponseError(err);
          callback();
        });
    }
  };

  /**
   * 检查域名是否符合规则
   * @type {Function}
   */
  checkDomain = _.debounce((rule, value, callback) => {
    const {
      intl: { formatMessage },
      form: { getFieldValue },
    } = this.props;
    const pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (pattern.test(value)) {
      const paths = getFieldValue('paths');
      const fields = [];
      _.forEach(paths, (item) => fields.push(`path[${item}]`));
      this.triggerPathCheck();
      callback();
    } else {
      callback(formatMessage({ id: 'domain.domain.check.failed' }));
    }
  }, 1000);

  /**
   * 校验端口
   * @param rule
   * @param value
   * @param callback
   */
  checkPorts = (rule, value, callback) => {
    const {
      intl: { formatMessage },
      form: { getFieldValue },
    } = this.props;
    if (value && getFieldValue('port') && !getFieldValue('port').includes(value)) {
      callback(formatMessage({ id: 'domain.network.check.failed' }));
    } else {
      callback();
    }
  };

  /**
   * 添加路径
   */
  addPath = () => {
    const {
      form: { getFieldValue, setFieldsValue },
    } = this.props;
    const keys = getFieldValue('paths');
    const uuid = this.pathKeys;
    const nextKeys = _.concat(keys, uuid);
    this.pathKeys = uuid + 1;
    setFieldsValue({
      paths: nextKeys,
    });
    this.setState({ pathCountChange: true });
  };

  /**
   * 删除路径
   * @param k
   */
  removePath = (k) => {
    const {
      form: { getFieldValue, setFieldsValue },
    } = this.props;
    const keys = getFieldValue('paths');
    if (keys.length === 1) {
      return;
    }
    setFieldsValue({
      paths: _.filter(keys, (key) => key !== k),
    });
    this.setState({ pathCountChange: true });
  };

  /**
   * 触发路径检查
   */
  triggerPathCheck = () => {
    const {
      form: { getFieldValue, validateFields },
    } = this.props;
    const paths = getFieldValue('paths');
    const fields = [];
    _.forEach(paths, (item) => fields.push(`path[${item}]`));
    validateFields(fields, { force: true });
    this.setState({ pathCountChange: false });
  };

  /**
   * 切换网络协议
   * @param e
   */
  handleTypeChange = (e) => {
    const {
      form: { getFieldValue, getFieldError, setFieldsValue },
    } = this.props;

    const protocol = e.target.value;

    const domain = getFieldValue('domain');
    if (domain && !getFieldError('domain')) {
      this.loadCertByDomain(domain, protocol);
    }

    this.setState({ protocol });
  };

  /**
   * 域名输入框失焦，查询证书
   * @param e
   * @param p 协议类型
   */
  loadCertByDomain = (e, p) => {
    const {
      form: { isModifiedField, resetFields },
      AppState: { currentMenuType: { projectId } },
      DomainStore,
      envId,
    } = this.props;
    const { protocol } = this.state;

    const value = e.target ? e.target.value : e;
    const type = p || protocol;

    if (isModifiedField('domain')) {
      resetFields('certId');
    }

    if (type === 'secret' && envId) {
      DomainStore.loadCertByEnv(projectId, envId, value);
    }
  };

  render() {
    const {
      form: {
        getFieldDecorator,
        getFieldValue,
        getFieldsError,
      },
      intl: { formatMessage },
      DomainStore,
      envId,
    } = this.props;
    const {
      protocol,
    } = this.state;

    const { getCertificates } = DomainStore;
    getFieldDecorator('paths', { initialValue: [0] });
    const paths = getFieldValue('paths');
    // 是否还存在校验未通过的path值
    const pathsError = getFieldsError(_.map(paths, (item) => `path[${item}]`));
    let hasPathError = true;
    _.forEach(pathsError, (pv, pk) => {
      if (pv[0]) {
        hasPathError = true;
      } else {
        hasPathError = false;
      }
    });
    // 生成路径-网络-端口的表单组
    const pathItem = _.map(paths, (k, index) => (
      <div className="domain-network-wrap" key={`paths-${k}`}>
        <FormItem
          className="domain-network-item c7ncd-domain-path"
          {...formItemLayout}
        >
          {getFieldDecorator(`path[${k}]`, {
            rules: [
              {
                validator: this.checkPath,
              },
            ],
            initialValue: '/',
          })(
            <Input
              onChange={() => this.setState({ pathCountChange: true })}
              disabled={!getFieldValue('domain')}
              maxLength={30}
              label={formatMessage({ id: 'domain.column.path' })}
              size="default"
            />,
          )}
        </FormItem>
        <FormItem
          className="domain-network-item c7n-select_160"
          {...formItemLayout}
        >
          {getFieldDecorator(`network[${k}]`, {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
            ],
            initialValue: getFieldValue('networkName'),
          })(
            <Input
              disabled
              label={formatMessage({ id: 'domain.column.network' })}
            />,
          )}
        </FormItem>
        <FormItem
          className="domain-network-item c7ncd-domain-port"
          {...formItemLayout}
        >
          {getFieldDecorator(`netPort[${k}]`, {
            trigger: ['onChange', 'onSubmit'],
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: this.checkPorts,
              },
            ],
          })(
            <Select
              getPopupContainer={(triggerNode) => triggerNode.parentNode}
              disabled={!getFieldValue(`network[${k}]`)}
              label={formatMessage({ id: 'domain.column.port' })}
              showSearch
              dropdownMatchSelectWidth
              size="default"
              optionLabelProp="children"
            >
              {_.map(_.filter(getFieldValue('port')), (item) => (
                <Option key={item} value={item}>
                  {item}
                </Option>
              ))}
            </Select>,
          )}
        </FormItem>
        {paths.length > 1 && (
          <Button
            shape="circle"
            className="c7n-domain-icon-delete"
            onClick={this.removePath.bind(this, k)}
          >
            <i className="icon icon-delete" />
          </Button>
        )}
      </div>
    ));

    return (
      <Form className="c7ncd-deploy-domain-form">
        <FormItem
          {...formItemLayout}
        >
          {getFieldDecorator('domainName', {
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
              disabled={!envId}
              maxLength={40}
              label={formatMessage({ id: 'domain.column.name' })}
            />,
          )}
        </FormItem>
        <div className="c7n-creation-title">
          <FormattedMessage id="domain.protocol.type" />
        </div>
        <FormItem
          className="creation-radio-form"
          label={<FormattedMessage id="ctf.target.type" />}
          {...formItemLayout}
        >
          {getFieldDecorator('type', {
            initialValue: protocol,
          })(
            <RadioGroup
              disabled={!envId}
              name="type"
              onChange={this.handleTypeChange}
            >
              <Radio value="normal">
                <FormattedMessage id="domain.protocol.normal" />
              </Radio>
              <Radio value="secret">
                <FormattedMessage id="domain.protocol.secret" />
              </Radio>
            </RadioGroup>,
          )}
        </FormItem>
        <FormItem
          className="creation-form-item"
          {...formItemLayout}
        >
          {getFieldDecorator('domain', {
            rules: [
              {
                required: true,
                whitespace: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: this.checkDomain,
              },
            ],
          })(
            <Input
              disabled={!envId}
              maxLength={50}
              type="text"
              label={formatMessage({ id: 'domain.form.domain' })}
              onBlur={this.loadCertByDomain}
            />,
          )}
        </FormItem>
        {protocol === 'secret' && (
          <FormItem
            className="creation-form-item"
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
                optionFilterProp="children"
                label={<FormattedMessage id="domain.form.cert" />}
                notFoundContent={
                  <FormattedMessage id="domain.cert.none" />
                }
                getPopupContainer={(triggerNode) => triggerNode.parentNode}
                filterOption={(input, option) => option.props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0}
                filter
                showSearch
              >
                {_.map(getCertificates, (item) => (
                  <Option value={item.id} key={item.id}>
                    {item.certName}
                  </Option>
                ))}
              </Select>,
            )}
          </FormItem>
        )}
        {pathItem}
        <div className="c7n-domain-btn-wrapper">
          <Tooltip
            title={
              hasPathError || !getFieldValue('domain')
                ? formatMessage({ id: 'domain.path.isnull' })
                : ''
            }
          >
            <Button
              className="c7n-domain-btn"
              onClick={this.addPath}
              type="primary"
              disabled={hasPathError || !getFieldValue('domain')}
              icon="add"
            >
              {formatMessage({ id: 'domain.path.add' })}
            </Button>
          </Tooltip>
        </div>
      </Form>
    );
  }
}

Index.propTypes = {
  envId: PropTypes.number,
  ingressId: PropTypes.number,
  type: PropTypes.string,
  isInstancePage: PropTypes.bool,
};

Index.defaultProps = {
  type: 'create',
};
