import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Tooltip, Radio, Input, Form, Select, Icon } from 'choerodon-ui';
import _ from 'lodash';
import PropTypes from 'prop-types';

import '../../../../../../../main.less';
import './index.scss';

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

@Form.create({})
@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Index extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deletedService: {},
      portInNetwork: {},
      protocol: 'normal',
      pathCountChange: false,
      selectEnv: props.envId,
      singleData: {},
    };
    this.pathKeys = 1;
  }

  componentDidMount() {
    const {
      intl: { formatMessage },
      AppState: { currentMenuType: { projectId } },
      type,
      form: { setFieldsValue, setFields },
      ingressId,
      DomainStore,
    } = this.props;
    if (ingressId && type === 'edit') {
      DomainStore.loadDataById(projectId, ingressId)
        .then((data) => {
          const {
            pathList,
            envId: domainEnv,
            certId,
            certName,
            domain,
          } = data;
          const deletedService = [];
          _.forEach(pathList, (item, index) => {
            const { serviceStatus, serviceName, serviceId } = item;
            if (serviceStatus !== 'running') {
              deletedService[index] = {
                name: serviceName,
                id: serviceId,
                status: serviceStatus,
              };
            } else {
              deletedService[index] = {};
            }
          });
          this.setState({
            deletedService,
            protocol: certId ? 'secret' : 'normal',
            singleData: data,
          });
          if (certId && domain && domainEnv) {
            DomainStore.loadCertByEnv(projectId, domainEnv, domain);
            if (certName) {
              setFieldsValue({ certId });
            } else {
              setFields({
                certId: {
                  value: null,
                  errors: [
                    new Error(formatMessage({ id: 'domain.cert.delete' })),
                  ],
                },
              });
            }
          }
        });
    }
  }

  /**
   * 添加或删除包含路径的表单项触发路径的校验
   * 确保 新添加 的一组路径已经渲染在页面上
   */
  componentDidUpdate() {
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
    } = this.props;
    const {
      selectEnv,
      singleData: { name },
    } = this.state;
    const p = /^([a-z0-9]([-a-z0-9]?[a-z0-9])*)$/;
    if (name && name === value) {
      callback();
    } else if (p.test(value)) {
      if (selectEnv) {
        DomainStore
          .checkName(projectId, value, selectEnv)
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
    } = this.props;
    const {
      selectEnv,
      singleData: { id },
    } = this.state;
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
            let checkPromise = null;
            if (type === 'edit') {
              checkPromise = DomainStore.checkPath(
                projectId,
                domain,
                selectEnv,
                encodeURIComponent(value),
                id,
              );
            } else {
              checkPromise = DomainStore.checkPath(
                projectId,
                domain,
                selectEnv,
                encodeURIComponent(value),
              );
            }
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
   * 校验网络是否可用
   * @param rule
   * @param value
   * @param callback
   */
  checkService = (rule, value, callback) => {
    const { type, intl: { formatMessage } } = this.props;
    const { deletedService } = this.state;
    if (type === 'create') {
      callback();
    } else {
      // network[xxx]
      const index = parseInt(rule.field.slice(8, -1), 10);
      const del = deletedService[index];
      if (del && del.id && del.id === value) {
        callback(formatMessage({ id: 'domain.network.check.failed' }));
      } else {
        callback();
      }
    }
  };

  /**
   * 校验端口
   * @param ports
   * @param rule
   * @param value
   * @param callback
   */
  checkPorts = (ports, rule, value, callback) => {
    const { intl: { formatMessage } } = this.props;
    if (ports && !ports.includes(value)) {
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
   * 根据网络加载端口
   * @param data
   * @param index 标识第几组的网络
   * @param id
   */
  handleSelectNetwork = (data, index, id) => {
    const {
      form: { setFieldsValue },
    } = this.props;
    const portArr = [];
    _.forEach(data, (item) => {
      if (id === item.id) {
        const {
          config: { ports },
        } = item;
        _.forEach(ports, (p) => portArr.push(p.port));
      }
    });
    setFieldsValue({ [`port[${index}]`]: '' });
    const portInNetwork = {
      [index]: portArr,
    };
    this.setState({ portInNetwork });
  };

  /**
   * 切换网络协议
   * @param e
   */
  handleTypeChange = (e) => {
    const {
      form: { getFieldValue, getFieldError, setFieldsValue },
    } = this.props;
    const { singleData } = this.state;

    const protocol = e.target.value;

    const domain = getFieldValue('domain');
    if (domain && !getFieldError('domain')) {
      this.loadCertByDomain(domain, protocol);
    }

    this.setState({ protocol }, () => {
      const { certId, domain: oldDomain } = singleData;
      if (protocol === 'secret' && certId && oldDomain && oldDomain === domain) {
        setFieldsValue({ certId });
      }
    });
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
    } = this.props;
    const { protocol, selectEnv } = this.state;

    const value = e.target ? e.target.value : e;
    const type = p || protocol;

    if (isModifiedField('domain')) {
      resetFields('certId');
    }

    if (type === 'secret' && selectEnv) {
      DomainStore.loadCertByEnv(projectId, selectEnv, value);
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
      type,
      isInstancePage,
      DomainStore,
    } = this.props;
    const {
      portInNetwork,
      protocol,
      deletedService,
      selectEnv,
      singleData: {
        pathList,
        name,
        domain,
      },
    } = this.state;

    const { getNetwork, getCertificates } = DomainStore;
    const network = getNetwork;
    let initPaths = [0];
    if (pathList && pathList.length) {
      initPaths.pop();
      initPaths = _.map(pathList, (item, index) => index);
      if (initPaths.length !== 1 && this.pathKeys === 1) {
        this.pathKeys = pathList.length;
      }
    }
    getFieldDecorator('paths', { initialValue: initPaths });
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
    const pathItem = _.map(paths, (k, index) => {
      let delNetOption = null;
      if (deletedService[k] && !_.isEmpty(deletedService[k])) {
        const { id, status, name: delNetName } = deletedService[k];
        delNetOption = (
          <Option value={id} key={`${id}-network-error`}>
            <div
              className={`c7n-domain-create-status c7n-domain-create-status_${status}`}
            >
              <div>{formatMessage({ id: status })}</div>
            </div>
            {delNetName}
          </Option>
        );
      }
      const hasServerInit = pathList && pathList.length && pathList[k];
      const initPort = hasServerInit ? pathList[k].servicePort : undefined;
      const initNetwork = hasServerInit ? pathList[k].serviceId : undefined;
      const initPath = hasServerInit ? pathList[k].path : '/';
      // 网络拥有的端口
      const portWithNetwork = {};
      _.forEach(network, (item) => {
        const {
          config: { ports },
          id,
        } = item;
        const port = [];
        _.forEach(ports, (p) => port.push(p.port));
        portWithNetwork[id] = port;
      });
      // 生成端口选项
      const portOption = type === 'edit' && !portInNetwork[k] && hasServerInit
        ? portWithNetwork[pathList[k].serviceId]
        : portInNetwork[k];
      // 生成网络选项
      const networkOption = _.map(network, ({ id, name: networkName }) => (
        <Option value={id} key={`${id}-network`}>
          <div className="c7n-domain-create-status c7n-domain-create-status_running">
            <div>{formatMessage({ id: 'running' })}</div>
          </div>
          <Tooltip title={networkName}>{networkName}</Tooltip>
        </Option>
      ));
      return (
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
              initialValue: initPath,
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
                {
                  validator: this.checkService,
                },
              ],
              initialValue: networkOption.length ? initNetwork : undefined,
            })(
              <Select
                getPopupContainer={(triggerNode) => triggerNode.parentNode}
                disabled={!selectEnv}
                filter
                label={formatMessage({ id: 'domain.column.network' })}
                showSearch
                dropdownMatchSelectWidth
                onSelect={this.handleSelectNetwork.bind(this, network, k)}
                size="default"
                optionFilterProp="children"
                optionLabelProp="children"
                filterOption={(input, option) => option.props.children[1].props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0}
              >
                {delNetOption}
                {networkOption}
              </Select>,
            )}
          </FormItem>
          <FormItem
            className="domain-network-item c7ncd-domain-port"
            {...formItemLayout}
          >
            {getFieldDecorator(`port[${k}]`, {
              trigger: ['onChange', 'onSubmit'],
              rules: [
                {
                  required: true,
                  message: formatMessage({ id: 'required' }),
                },
                {
                  validator: this.checkPorts.bind(this, portOption),
                },
              ],
              initialValue: initPort,
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
                {_.map(portOption, (item) => (
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
      );
    });

    return (
      <Form layout="vertical" className="c7ncd-application-domain-modal">
        <FormItem
          className="c7n-domain-formItem c7n-select_512"
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
            initialValue: name || '',
          })(
            <Input
              autoFocus={type === 'create'}
              disabled={!selectEnv}
              maxLength={40}
              label={formatMessage({ id: 'domain.column.name' })}
            />,
          )}
        </FormItem>
        <div className="c7n-creation-title">
          <FormattedMessage id="domain.protocol" />
        </div>
        <div className="c7n-creation-radio">
          <FormItem
            className="c7n-select_512 creation-radio-form"
            label={<FormattedMessage id="ctf.target.type" />}
            {...formItemLayout}
          >
            {getFieldDecorator('type', {
              initialValue: protocol,
            })(
              <RadioGroup
                disabled={!selectEnv}
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
        </div>
        <FormItem
          className="c7n-select_480 creation-form-item"
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
            initialValue: domain || '',
          })(
            <Input
              disabled={!selectEnv}
              maxLength={50}
              type="text"
              label={formatMessage({ id: 'domain.form.domain' })}
              size="default"
              onBlur={this.loadCertByDomain}
            />,
          )}
        </FormItem>
        {protocol === 'secret' && (
          <FormItem
            className="c7n-select_480 creation-form-item"
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
                className="c7n-select_512"
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
