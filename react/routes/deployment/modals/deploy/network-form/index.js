import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button,
  Form,
  Select,
  Input,
  Popover,
  Icon,
  Radio,
} from 'choerodon-ui';
import classnames from 'classnames';
import _ from 'lodash';

import './index.less';

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

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class CreateNetwork extends Component {
  constructor(props) {
    super(props);
    /* **************
     *                        state              this
     * portKeys/targetKeys | 用于radio选择模式 | 生成一组表单项的唯一表示
     *
     ************** */
    this.state = {
      targetKeys: 'instance',
      portKeys: 'ClusterIP',
      initName: '',
      validIp: {},
      targetIp: {},
    };
    this.portKeys = 1;
    this.targetKeys = 0;
    this.endPoints = 0;
  }

  /**
   * 检查名字的唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      intl: { formatMessage },
      store,
      AppState: { currentMenuType: { projectId } },
      envId,
    } = this.props;
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      callback(formatMessage({ id: 'network.name.check.failed' }));
    } else if (value && pattern.test(value)) {
      store.checkNetWorkName(projectId, envId, value)
        .then((data) => {
          if (data) {
            callback();
          } else {
            callback(formatMessage({ id: 'network.name.check.exist' }));
          }
        });
    } else {
      callback();
    }
  }, 1000);

  /**
   * 验证ip
   * @param rule
   * @param value
   * @param callback
   */
  checkIP = (rule, value, callback, type) => {
    const { intl: { formatMessage } } = this.props;
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    const validIp = {};
    const data = type === 'targetIps' ? 'targetIp' : 'validIp';
    let errorMsg;
    if (value && value.length) {
      _.forEach(value, (item) => {
        if (!p.test(item)) {
          errorMsg = formatMessage({ id: 'network.ip.check.failed' });
          validIp[item] = true;
        }
      });
      this.setState({ [data]: validIp });
      callback(errorMsg);
    } else {
      callback();
    }
  };

  /**
   * 验证端口号
   * @param rule
   * @param value
   * @param callback
   * @param type
   */
  checkPort = (rule, value, callback, type) => {
    const {
      intl: { formatMessage },
      form: { getFieldValue },
    } = this.props;
    const p = /^([1-9]\d*|0)$/;
    const count = _.countBy(getFieldValue(type));
    const data = {
      typeMsg: '',
      min: 0,
      max: 65535,
      failedMsg: 'network.port.check.failed',
    };
    switch (type) {
      case 'tport':
        data.typeMsg = 'network.tport.check.repeat';
        break;
      case 'nport':
        data.typeMsg = 'network.nport.check.repeat';
        data.min = 30000;
        data.max = 32767;
        data.failedMsg = 'network.nport.check.failed';
        break;
      case 'targetport':
        data.typeMsg = 'network.tport.check.repeat';
        break;
      default:
        data.typeMsg = 'network.port.check.repeat';
    }
    if (value) {
      if (
        p.test(value)
        && parseInt(value, 10) >= data.min
        && parseInt(value, 10) <= data.max
      ) {
        if (count[value] < 2) {
          callback();
        } else {
          callback(formatMessage({ id: data.typeMsg }));
        }
      } else {
        callback(formatMessage({ id: data.failedMsg }));
      }
    } else {
      callback();
    }
  };

  /**
   * 目标和网络配置类型选择
   * @param e
   * @param key
   */
  handleTypeChange = (e, key) => {
    const {
      form: {
        resetFields,
        setFieldsValue,
      },
    } = this.props;

    this.portKeys = 1;
    resetFields(['port', 'tport', 'nport', 'protocol']);
    setFieldsValue({
      [key]: [0],
    });

    this.setState({ [key]: e.target.value });
  };

  /**
   * 移除一组表单项
   * @param k
   * @param type
   */
  removeGroup = (k, type) => {
    const {
      form: {
        getFieldValue,
        setFieldsValue,
        validateFields,
      },
      store,
    } = this.props;
    const { portKeys } = this.state;
    const keys = getFieldValue(type);
    if (keys.length === 1) {
      return;
    }

    let list = [];
    switch (type) {
      case 'portKeys':
        list = ['port', 'tport'];
        portKeys !== 'ClusterIP' && list.push('nport');
        break;
      case 'endPoints':
        list = ['targetport'];
        break;
      case 'targetKeys':
        list = ['keywords'];
        break;
      default:
        break;
    }

    setFieldsValue({
      [type]: _.filter(keys, (key) => key !== k),
    }, () => {
      validateFields(list, { force: true });
      this.triggerNetPortCheck();
    });
  };

  /**
   * 动态生成一组表单项
   * @param type
   */
  addGroup = (type) => {
    const {
      form: {
        getFieldValue,
        setFieldsValue,
      },
    } = this.props;
    const keys = getFieldValue(type);
    const uuid = this[type];
    const nextKeys = _.concat(keys, uuid);
    this[type] = uuid + 1;
    setFieldsValue({
      [type]: nextKeys,
    });
  };

  /**
   * 每当节点端口、端口、目标端口、关键字等输入改变，强制校验，消除重复的报错信息
   */
  changeValue = _.debounce((type, keyFiled, valueFiled, value) => {
    const {
      form: {
        validateFields,
      },
    } = this.props;

    if (type === 'keywords') {
      this.selectLabel(value, keyFiled, valueFiled);
    }

    if (type === 'port') {
      this.triggerNetPortCheck();
    }

    validateFields([type], { force: true });
  }, 400);

  triggerNetPortCheck = () => {
    const {
      form: {
        validateFields,
        getFieldValue,
        setFieldsValue,
      },
    } = this.props;
    _.forEach(getFieldValue('paths'), (item) => {
      const value = getFieldValue(`netPort[${item}]`);
      if ((value && !getFieldValue('port').includes(value))) {
        setFieldsValue({ [`netPort[${item}]`]: '' });
      }
    });
  };

  /**
   * 处理输入的内容并返回给value
   * @param liNode
   * @param value
   * @returns {*}
   */
  handleChoiceRender = (liNode, value, type) => React.cloneElement(liNode, {
    className: classnames(liNode.props.className, {
      'ip-check-error': this.state[type || 'validIp'][value],
    }),
  });

  /**
   * 删除ip选择框中的标签校验标识
   * @param value
   */
  handleChoiceRemove = (value, type) => {
    const data = this.state[type || 'validIp'];
    // 直接删除
    if (value in data) {
      delete data[value];
    }
  };

  /**
   * ip选择框监听键盘按下事件
   * @param e
   */
  handleInputKeyDown = (e, type) => {
    const { value } = e.target;
    if (e.keyCode === 13 && !e.isDefaultPrevented() && value) {
      this.setIpInSelect(value, type);
    }
  };

  setIpInSelect = (value, type) => {
    const {
      form: {
        getFieldValue,
        validateFields,
        setFieldsValue,
      },
    } = this.props;
    const itemType = type || 'externalIps';
    const ip = getFieldValue(itemType) || [];
    if (!ip.includes(value)) {
      ip.push(value);
      setFieldsValue({
        [itemType]: ip,
      });
    }
    validateFields([itemType]);
    const data = type === 'targetIps' ? this.targetIpSelect : this.ipSelect;
    if (data) {
      data.setInputValue('');
    }
  };

  ipSelectRef = (node, type) => {
    const data = type === 'targetIps' ? 'targetIpSelect' : 'ipSelect';
    if (node) {
      this[data] = node.rcSelect;
    }
  };

  selectLabel = (value, keyFiled, valueFiled) => {
    const { form: { setFieldsValue, validateFields } } = this.props;
    if (_.includes(value, '__')) {
      setFieldsValue({
        [keyFiled]: value.split('__')[0],
        [valueFiled]: value.split('__')[1],
      });
      validateFields(['keywords'], { force: true });
    }
  };

  handleChangeName = (e) => {
    const {
      store,
      form: {
        getFieldValue,
        setFieldsValue,
      },
    } = this.props;
    _.forEach(getFieldValue('paths'), (item) => {
      setFieldsValue({
        [`network[${item}]`]: e.target.value,
      });
    });
  };

  render() {
    const {
      form: {
        getFieldDecorator,
        getFieldValue,
      },
      intl: { formatMessage },
      store,
      envId,
    } = this.props;
    const {
      portKeys: configType,
      initName,
    } = this.state;

    // 生成多组 port
    getFieldDecorator('portKeys', { initialValue: [0] });
    const portKeys = getFieldValue('portKeys');
    const portItems = _.map(portKeys, (k) => (
      <div key={`port-${k}`} className="network-port-wrap">
        {configType !== 'ClusterIP' && (
          <FormItem
            className="c7n-select_80 network-panel-form network-port-form"
            {...formItemLayout}
          >
            {getFieldDecorator(`nport[${k}]`, {
              rules: [
                {
                  validator: (rule, value, callback) => this.checkPort(rule, value, callback, 'nport'),
                },
              ],
            })(
              <Input
                type="text"
                maxLength={5}
                onChange={this.changeValue.bind(this, 'nport')}
                label={<FormattedMessage id="network.config.nodePort" />}
              />,
            )}
          </FormItem>
        )}
        <FormItem
          className="c7n-select_80 network-panel-form network-port-form"
          {...formItemLayout}
        >
          {getFieldDecorator(`port[${k}]`, {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: (rule, value, callback) => this.checkPort(rule, value, callback, 'port'),
              },
            ],
          })(
            <Input
              type="text"
              maxLength={5}
              onChange={this.changeValue.bind(this, 'port')}
              label={<FormattedMessage id="network.config.port" />}
            />,
          )}
        </FormItem>
        <FormItem
          className="c7n-select_80 network-panel-form network-port-form"
          {...formItemLayout}
        >
          {getFieldDecorator(`tport[${k}]`, {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: (rule, value, callback) => this.checkPort(rule, value, callback, 'tport'),
              },
            ],
          })(
            <Input
              type="text"
              maxLength={5}
              onChange={this.changeValue.bind(this, 'tport')}
              label={<FormattedMessage id="network.config.targetPort" />}
            />,
          )}
        </FormItem>
        {configType === 'NodePort' && (
          <FormItem
            className="c7n-select_80 network-panel-form network-port-form"
            {...formItemLayout}
          >
            {getFieldDecorator(`protocol[${k}]`, {
              rules: [
                {
                  required: true,
                  message: formatMessage({ id: 'required' }),
                },
              ],
            })(
              <Select
                label={<FormattedMessage id="ist.deploy.ports.protocol" />}
              >
                {_.map(['TCP', 'UDP'], (item) => (
                  <Option value={item} key={item}>
                    {item}
                  </Option>
                ))}
              </Select>,
            )}
          </FormItem>
        )}
        {portKeys.length > 1 && (
          <Icon
            className="network-group-icon"
            type="delete"
            onClick={() => this.removeGroup(k, 'portKeys')}
          />
        )}
      </div>
    ));

    return (
      <Form
        layout="horizontal"
        className="c7ncd-deploy-network-form-wrap"
        style={{ width: '66.66%' }}
      >
        <FormItem
          className="network-form-name"
          {...formItemLayout}
        >
          {getFieldDecorator('networkName', {
            initialValue: initName,
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
              maxLength={30}
              disabled={!envId}
              type="text"
              label={<FormattedMessage id="network.form.name" />}
              onChange={this.handleChangeName}
            />,
          )}
        </FormItem>
        <div className="network-panel-title">
          <FormattedMessage id="ist.networking.service.type" />
        </div>
        <div>
          <FormItem
            className="network-radio-form"
            {...formItemLayout}
          >
            {getFieldDecorator('config', {
              initialValue: configType,
            })(
              <RadioGroup
                name="config"
                onChange={(e) => this.handleTypeChange(e, 'portKeys')}
              >
                <Radio value="ClusterIP">ClusterIP</Radio>
                <Radio value="NodePort">NodePort</Radio>
                <Radio value="LoadBalancer">LoadBalancer</Radio>
              </RadioGroup>,
            )}
          </FormItem>
          <div className="network-panel">
            {configType === 'ClusterIP' && (
              <FormItem
                className="network-panel-form"
                {...formItemLayout}
              >
                {getFieldDecorator('externalIps', {
                  rules: [
                    {
                      validator: this.checkIP,
                    },
                  ],
                })(
                  <Select
                    mode="tags"
                    ref={this.ipSelectRef}
                    label={<FormattedMessage id="network.config.ip" />}
                    onInputKeyDown={this.handleInputKeyDown}
                    choiceRender={this.handleChoiceRender}
                    onChoiceRemove={this.handleChoiceRemove}
                    filterOption={false}
                    notFoundContent={false}
                    showNotFindInputItem={false}
                    showNotFindSelectedItem={false}
                    allowClear
                  />,
                )}
              </FormItem>
            )}
            {portItems}
            <Button
              type="primary"
              funcType="flat"
              onClick={() => this.addGroup('portKeys')}
              icon="add"
            >
              <FormattedMessage id="network.config.addport" />
            </Button>
          </div>
        </div>
      </Form>
    );
  }
}
