/* eslint-disable no-useless-return */
import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from "react-intl";
import {
  Button,
  Form,
  Select,
  Input,
  Modal,
  Popover,
  Icon,
  Radio,
} from "choerodon-ui";
import { stores, Content } from "@choerodon/boot";
import uuidv1 from "uuid/v1";
import classnames from "classnames";
import _ from "lodash";
import "../../../main.scss";
import "./CreateNetwork.scss";
import EnvOverviewStore from "../../../../stores/project/envOverview";
import AppName from "../../../../components/appName";
import InterceptMask from "../../../../components/interceptMask/InterceptMask";
import Tips from "../../../../components/Tips/Tips";

/**
 * 生成网络名
 * @param opt
 * @returns {string}
 */
function createNetworkName(opt) {
  let initName = opt.key;
  if (initName.length > 23) {
    // 初始网络名长度限制
    initName = initName.slice(0, 23);
  }
  initName = `${initName}-${uuidv1().slice(0, 6)}`;
  return initName;
}

const { AppState } = stores;
const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { Option, OptGroup } = Select;
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

@observer
class CreateNetwork extends Component {
  constructor(props) {
    super(props);
    /* **************
     *                        state              this
     * portKeys/targetKeys | 用于radio选择模式 | 生成一组表单项的唯一表示
     *
     ************** */
    this.state = {
      submitting: false,
      targetKeys: "instance",
      portKeys: "ClusterIP",
      initName: "",
      validIp: {},
      targetIp: {},
      initIst: [],
      initIstOption: [],
    };
    this.portKeys = 1;
    this.targetKeys = 0;
    this.endPoints = 0;
  }

  componentDidMount() {
    const { envId, appId, appCode, form } = this.props;
    const { id } = AppState.currentMenuType;
    form.resetFields();
    if (envId) {
      const options = { key: appCode };
      this.handleEnvSelect(envId);
      if (appId) {
        this.handleAppSelect(appId, options);
        this.loadIstById();
      }
    }
    EnvOverviewStore.loadActiveEnv(id);
  }

  setIpInSelect = (value, type) => {
    const { getFieldValue, validateFields, setFieldsValue } = this.props.form;
    const itemType = type || "externalIps";
    const ip = getFieldValue(itemType) || [];
    if (!ip.includes(value)) {
      ip.push(value);
      setFieldsValue({
        [itemType]: ip,
      });
    }
    validateFields([itemType]);
    const data = type === "targetIps" ? this.targetIpSelect : this.ipSelect;
    if (data) {
      data.setInputValue("");
    }
  };

  handleSubmit = e => {
    e.preventDefault();

    const { form, store } = this.props;
    const { id } = AppState.currentMenuType;
    this.setState({ submitting: true });

    let enableSubmit = true;

    const _changeStatus = (flag) => {
      enableSubmit = flag;
    };

    const _pushUnRecordIp = (ips, ip) => {
      if (ip) {
        ips.push(ip);
      }
    };

    /**
     * 当外部ip的输入值状态不为空（指已有输入值但是未生成标签填入该表单项时)
     * 直接点击提交后，form组件无法获取外部ip填入的最后值
     * 通过判断外部ip的inputValue字段是否为空来判断是否有未记录的输入值
     */
    const _unInputIp = this.checkUnRecordIp(this.ipSelect, _changeStatus);
    const _unInputEndIp = this.checkUnRecordIp(this.targetIpSelect, _changeStatus);

    form.validateFieldsAndScroll((err, data) => {
      if (!err && enableSubmit) {
        const {
          name,
          appId,
          appInstance,
          envId,
          endPoints: endps,
          targetIps,
          targetport,
          externalIps,
          portKeys,
          port,
          tport,
          nport,
          protocol,
          targetKeys,
          keywords,
          config,
          values,
        } = data;
        const appIst = appInstance ? _.map(appInstance, item => item) : null;
        const ports = [];
        const label = {};
        const endPoints = {};

        const _externalIps = externalIps || [];
        const _targetIps = targetIps || [];
        _pushUnRecordIp(_externalIps, _unInputIp);
        _pushUnRecordIp(_targetIps, _unInputEndIp);

        if (portKeys) {
          _.forEach(portKeys, item => {
            if (item || item === 0) {
              const node = {
                port: Number(port[item]),
                targetPort: Number(tport[item]),
                nodePort: nport ? Number(nport[item]) : null,
              };
              config === 'NodePort' && (node.protocol = protocol[item]);
              ports.push(node);
            }
          });
        }

        if (targetKeys) {
          _.forEach(targetKeys, item => {
            if (item || item === 0) {
              const key = keywords[item];
              label[key] = values[item];
            }
          });
        }

        // targetIps是必填项，当输入值不为空，但是记录值为空时，点击提交会判断为空
        // 此时会改变为表单校验未通过，但是又会自动把输入值填入记录值，使该项正常
        // 造成需要点击两次才能提交。属于不当操作造成，暂未做处理
        if (endps && endps.length && _targetIps) {
          endPoints[_targetIps.join(",")] = _.map(
            _.filter(endps, item => item || item === 0),
            item => ({
              name: null,
              port: Number(targetport[item]),
            })
          );
        }

        const network = {
          name,
          appId: appId || null,
          appInstance: appIst,
          envId,
          externalIp: _externalIps.length ? _externalIps.join(",") : null,
          ports,
          label: !_.isEmpty(label) ? label : null,
          type: config,
          endPoints: !_.isEmpty(endPoints) ? endPoints : null,
        };

        store
          .createNetwork(id, network)
          .then(res => {
            this.setState({ submitting: false });
            if (res) {
              this.handleClose();
            }
          })
          .catch(error => {
            this.setState({ submitting: false });
            Choerodon.handleResponseError(error);
          });
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  loadIstById = () => {
    const { store, envId, appId, istId } = this.props;
    const { id } = AppState.currentMenuType;
    store.loadEnv(id);
    store.loadInstance(id, envId, appId).then(data => {
      if (data) {
        const initIst = [];
        // 将默认选项直接生成，避免加载带来的异步问题
        const initIstOption = [];
        if (data && data.length) {
          initIst.push(istId.toString());
          _.forEach(data, item => {
            const { id: istIds, code } = item;
            initIstOption.push(
              <Option key={istIds} value={istIds}>
                {code}
              </Option>
            );
          });
        }
        this.setState({
          initIst,
          initIstOption,
        });
      }
    });
  };

  handleClose = (isload = true) => {
    const { onClose } = this.props;
    onClose(isload);
  };

  /**
   * 环境选择，加载应用
   * 环境下可以部署多个应用
   * @param value
   */
  handleEnvSelect = value => {
    const envId = EnvOverviewStore.getTpEnvId;
    if (!value) {
      return;
    }
    EnvOverviewStore.setTpEnvId(value);
    const { store } = this.props;
    const { id } = AppState.currentMenuType;
    store.loadApp(id, Number(value));
    if (envId !== value) {
      const { resetFields } = this.props.form;
      resetFields(["appId", "appInstance"]);
    }
  };

  /**
   * 目标和网络配置类型选择
   * @param e
   * @param key
   */
  handleTypeChange = (e, key) => {
    const {
      getFieldValue,
      getFieldDecorator,
      resetFields,
      setFieldsValue,
    } = this.props.form;
    const keys = getFieldValue(key);
    if (key === "portKeys") {
      // 清除多组port映射
      this.portKeys = 1;
      // 清空表单项
      resetFields(["port", "tport", "nport", "protocol"]);
      setFieldsValue({
        [key]: [0],
      });
    } else {
      // 切换到“选择实例”时，清空标签、endPoints生成的表单项
      const value = e.target.value === "param" ? "targetKeys" : e.target.value;
      _.map(["targetKeys", "endPoints"], item => {
        if (value === item) {
          getFieldDecorator(item, { initialValue: [0] });
          this[item] = 1;
          setFieldsValue({
            [item]: [0],
          });
        } else {
          const list = {
            targetKeys: ["keywords", "values"],
            endPoints: ["targetport"],
          };
          this[item] = 0;
          getFieldDecorator(item, { initialValue: [] });
          setFieldsValue({
            [item]: [],
          });
          resetFields(list[item]);
        }
      });
    }
    this.setState({ [key]: e.target.value });
  };

  /**
   * 选择应用, 加载实例, 生成初始网络名
   * @param value
   * @param options
   */
  handleAppSelect = (value, options) => {
    const {
      store,
      form: { getFieldValue, setFieldsValue },
    } = this.props;
    const { id } = AppState.currentMenuType;
    const envId = getFieldValue("envId");
    const initName = createNetworkName(options);
    this.setState({ initName });
    setFieldsValue({
      appInstance: [],
    });
    store.loadInstance(id, envId, Number(value)).then(data => {
      if (data) {
        const initIst = [];
        // 将默认选项直接生成，避免加载带来的异步问题
        const initIstOption = [];
        if (data && data.length) {
          _.forEach(data, item => {
            const { id: istIds, code } = item;
            initIstOption.push(
              <Option key={istIds} value={code}>
                {code}
              </Option>
            );
          });
        }
        this.setState({
          initIst,
          initIstOption,
        });
      }
    });
  };

  /**
   * 移除一组表单项
   * @param k
   * @param type
   */
  removeGroup = (k, type) => {
    const {
      form: { getFieldValue, setFieldsValue },
    } = this.props;
    const keys = getFieldValue(type);
    if (keys.length === 1) {
      return;
    }
    setFieldsValue({
      [type]: _.filter(keys, key => key !== k),
    });
  };

  /**
   * 动态生成一组表单项
   * @param type
   */
  addGroup = type => {
    const { getFieldValue, setFieldsValue } = this.props.form;
    const keys = getFieldValue(type);
    const uuid = this[type];
    const nextKeys = _.concat(keys, uuid);
    this[type] = uuid + 1;
    setFieldsValue({
      [type]: nextKeys,
    });
  };

  /**
   * 生成app选项组
   * @param node
   * @returns {*}
   */
  makeAppGroup(node) {
    const { id, name, code, projectId } = node;
    const { id: currentProject } = AppState.currentMenuType;
    return (
      <Option value={id} key={code}>
        <Popover
          placement="right"
          content={
            <Fragment>
              <p>
                <FormattedMessage id="app.name" />:<span>{name}</span>
              </p>
              <p>
                <FormattedMessage id="app.code" />:<span>{code}</span>
              </p>
            </Fragment>
          }
        >
          <div className="c7ncd-net-app">
            <AppName
              name={name}
              showIcon
              self={projectId === Number(currentProject)}
              width="460px"
            />
          </div>
        </Popover>
      </Option>
    );
  }

  /**
   * 检查名字的唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const { intl, store, form } = this.props;
    const { id } = AppState.currentMenuType;
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    const envId = form.getFieldValue("envId");
    if (value && !pattern.test(value)) {
      callback(intl.formatMessage({ id: "network.name.check.failed" }));
    } else if (value && pattern.test(value)) {
      store.checkNetWorkName(id, envId, value).then(data => {
        if (data) {
          callback();
        } else {
          callback(intl.formatMessage({ id: "network.name.check.exist" }));
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
    const { intl } = this.props;
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    const validIp = {};
    const data = type === "targetIps" ? "targetIp" : "validIp";
    let errorMsg;
    if (value && value.length) {
      _.forEach(value, (item, index) => {
        if (!p.test(item)) {
          errorMsg = intl.formatMessage({ id: "network.ip.check.failed" });
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
   * 校验 form 组件无法记录的 ip
   * @param ref 组件的引用
   * @param callback 改变提交状态
   * @returns {*}
   */
  checkUnRecordIp = (ref, callback) => {
    const IP_EXPR = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    const ENABLE_SUBMIT = true;
    let unInputIp = null;

    if (ref && ref.state.inputValue) {
      const ipValue = ref.state.value;
      const ipInputValue = ref.state.inputValue;

      if (IP_EXPR.test(ipInputValue)) {
        if (!ipValue.includes(ipInputValue)) {
          unInputIp = ipInputValue;
        }
        callback(ENABLE_SUBMIT);
      } else {
        callback(!ENABLE_SUBMIT);
      }
    }

    return unInputIp;
  };

  /**
   * 验证端口号
   * @param rule
   * @param value
   * @param callback
   * @param type
   */
  checkPort = (rule, value, callback, type) => {
    const { intl, form } = this.props;
    const { getFieldValue } = form;
    const p = /^([1-9]\d*|0)$/;
    const count = _.countBy(getFieldValue(type));
    const data = {
      typeMsg: "",
      min: 0,
      max: 65535,
      failedMsg: "network.port.check.failed",
    };
    switch (type) {
      case "tport":
        data.typeMsg = "network.tport.check.repeat";
        break;
      case "nport":
        data.typeMsg = "network.nport.check.repeat";
        data.min = 30000;
        data.max = 32767;
        data.failedMsg = "network.nport.check.failed";
        break;
      case "targetport":
        data.typeMsg = "network.tport.check.repeat";
        break;
      default:
        data.typeMsg = "network.port.check.repeat";
    }
    if (value) {
      if (
        p.test(value) &&
        parseInt(value, 10) >= data.min &&
        parseInt(value, 10) <= data.max
      ) {
        if (count[value] < 2) {
          callback();
        } else {
          callback(intl.formatMessage({ id: data.typeMsg }));
        }
      } else {
        callback(intl.formatMessage({ id: data.failedMsg }));
      }
    } else {
      callback();
    }
  };

  /**
   * 关键字检查
   * @param rule
   * @param value
   * @param callback
   */
  checkKeywords = (rule, value, callback) => {
    const { intl } = this.props;
    const { getFieldValue } = this.props.form;
    // 必须由字母数字字符，' - '，'_'或'.'组成，并且必须以字母数字开头和结尾
    // 并且包括可选的DNS子域前缀(包括一级、二级域名)和'/'（例如'example.com/MyName'）
    const p = /^((?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\/)*([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$/;
    const keyCount = _.countBy(getFieldValue("keywords"));
    if (value) {
      if (p.test(value)) {
        if (keyCount[value] < 2) {
          callback();
        } else {
          callback(intl.formatMessage({ id: "network.label.check.repeat" }));
        }
      } else {
        callback(intl.formatMessage({ id: "network.label.check.failed" }));
      }
    } else {
      callback();
    }
  };

  checkValue = (rule, value, callback) => {
    const { intl } = this.props;
    const p = /^(([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?$/;
    if (value) {
      if (p.test(value)) {
        callback();
      } else {
        callback(intl.formatMessage({ id: "network.label.check.failed" }));
      }
    } else {
      callback();
    }
  };

  /**
   * 处理输入的内容并返回给value
   * @param liNode
   * @param value
   * @returns {*}
   */
  handleChoiceRender = (liNode, value, type) =>
    React.cloneElement(liNode, {
      className: classnames(liNode.props.className, {
        "ip-check-error": this.state[type || "validIp"][value],
      }),
    });

  /**
   * 删除ip选择框中的标签校验标识
   * @param value
   */
  handleChoiceRemove = (value, type) => {
    const data = this.state[type || "validIp"];
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

  /**
   * 获取环境选择器的元素节点
   * @param node
   */
  envSelectRef = node => {
    if (node) {
      this.envSelect = node.rcSelect;
    }
  };

  ipSelectRef = (node, type) => {
    const data = type === "targetIps" ? "targetIpSelect" : "ipSelect";
    if (node) {
      this[data] = node.rcSelect;
    }
  };

  render() {
    const { visible, form, intl, store } = this.props;
    const {
      submitting,
      targetKeys: targetType,
      portKeys: configType,
      initIst,
      initIstOption,
      initName,
    } = this.state;
    const { name: menuName, id: projectId } = AppState.currentMenuType;
    const { getFieldDecorator, getFieldValue } = form;
    const env = EnvOverviewStore.getEnvcard;
    const localApp = _.filter(
      store.getApp,
      item => item.projectId === Number(projectId)
    );
    const storeApp = _.filter(
      store.getApp,
      item => item.projectId !== Number(projectId)
    );
    const ist = store.getIst;
    let portWidthSingle = "240";
    let portWidthMut = "portL";
    if (configType !== "ClusterIP") {
      portWidthSingle = configType === "LoadBalancer" ? "150" : "110";
      portWidthMut = configType === "LoadBalancer" ? "portS" : "portXS";
    }
    // 生成多组 port
    getFieldDecorator("portKeys", { initialValue: [0] });
    const portKeys = getFieldValue("portKeys");
    const portItems = _.map(portKeys, (k, index) => (
      <div key={`port-${k}`} className="network-port-wrap">
        {configType !== "ClusterIP" ? (
          <FormItem
            className={`c7n-select_${
              portKeys.length > 1 ? portWidthMut : portWidthSingle
            } network-panel-form network-port-form`}
            {...formItemLayout}
          >
            {getFieldDecorator(`nport[${k}]`, {
              rules: [
                {
                  validator: (rule, value, callback) =>
                    this.checkPort(rule, value, callback, "nport"),
                },
              ],
            })(
              <Input
                type="text"
                maxLength={5}
                label={<FormattedMessage id="network.config.nodePort" />}
              />
            )}
          </FormItem>
        ) : null}
        <FormItem
          className={`c7n-select_${
            portKeys.length > 1 ? portWidthMut : portWidthSingle
          } network-panel-form network-port-form`}
          {...formItemLayout}
        >
          {getFieldDecorator(`port[${k}]`, {
            rules: [
              {
                required: true,
                message: intl.formatMessage({ id: "required" }),
              },
              {
                validator: (rule, value, callback) =>
                  this.checkPort(rule, value, callback, "port"),
              },
            ],
          })(
            <Input
              type="text"
              maxLength={5}
              disabled={!getFieldValue("envId")}
              label={<FormattedMessage id="network.config.port" />}
            />
          )}
        </FormItem>
        <FormItem
          className={`c7n-select_${
            portKeys.length > 1 ? portWidthMut : portWidthSingle
          } network-panel-form network-port-form`}
          {...formItemLayout}
        >
          {getFieldDecorator(`tport[${k}]`, {
            rules: [
              {
                required: true,
                message: intl.formatMessage({ id: "required" }),
              },
              {
                validator: (rule, value, callback) =>
                  this.checkPort(rule, value, callback, "tport"),
              },
            ],
          })(
            <Input
              type="text"
              maxLength={5}
              disabled={!getFieldValue("envId")}
              label={<FormattedMessage id="network.config.targetPort" />}
            />
          )}
        </FormItem>
        {configType === "NodePort" ? (
          <FormItem
            className={`c7n-select_${
              portKeys.length > 1 ? portWidthMut : portWidthSingle
              } network-panel-form network-port-form`}
            {...formItemLayout}
          >
            {getFieldDecorator(`protocol[${k}]`, {
              rules: [
                {
                  required: true,
                  message: intl.formatMessage({ id: "required" }),
                },
              ],
            })(
              <Select
                className="c7n-select_110"
                label={<FormattedMessage id="ist.deploy.ports.protocol" />}
              >
                {_.map(['TCP', 'UDP'], item => (
                  <Option value={item} key={item}>
                    {item}
                  </Option>
                ))}
              </Select>
            )}
          </FormItem>
        ) : null}
        {portKeys.length > 1 ? (
          <Icon
            className="network-group-icon"
            type="delete"
            onClick={() => this.removeGroup(k, "portKeys")}
          />
        ) : null}
      </div>
    ));

    // endPoints生成多组 port
    getFieldDecorator("endPoints");
    const endPoints = getFieldValue("endPoints");
    const targetPortItems = _.map(endPoints, (k, index) => (
      <div key={`endPoints-${k}`} className="network-port-wrap">
        <FormItem
          className={`c7n-select_480 network-panel-form network-port-form`}
          {...formItemLayout}
        >
          {getFieldDecorator(`targetport[${k}]`, {
            rules: [
              {
                required: true,
                message: intl.formatMessage({ id: "required" }),
              },
              {
                validator: (rule, value, callback) =>
                  this.checkPort(rule, value, callback, "targetport"),
              },
            ],
          })(
            <Input
              type="text"
              maxLength={5}
              disabled={!getFieldValue("envId")}
              label={<FormattedMessage id="network.config.targetPort" />}
            />
          )}
        </FormItem>
        {endPoints.length > 1 ? (
          <Icon
            className="network-group-icon"
            type="delete"
            onClick={() => this.removeGroup(k, "endPoints")}
          />
        ) : null}
      </div>
    ));

    // 生成多组 target
    getFieldDecorator("targetKeys");
    const targetKeys = getFieldValue("targetKeys");
    const targetItems = _.map(targetKeys, (k, index) => (
      <div key={`target-${k}`} className="network-port-wrap">
        <FormItem
          className={`c7n-select_${
            targetKeys.length > 1 ? "entryS" : "entryL"
          } network-panel-form network-port-form`}
          {...formItemLayout}
        >
          {getFieldDecorator(`keywords[${k}]`, {
            rules: [
              {
                required: true,
                message: intl.formatMessage({ id: "required" }),
              },
              {
                validator: this.checkKeywords,
              },
            ],
          })(
            <Input
              type="text"
              disabled={!getFieldValue("envId")}
              label={<FormattedMessage id="network.config.keyword" />}
              suffix={<Tips type="form" data="network.label.key.rule" />}
            />
          )}
        </FormItem>
        <Icon className="network-group-icon" type="drag_handle" />
        <FormItem
          className={`c7n-select_${
            targetKeys.length > 1 ? "entryS" : "entryL"
          } network-panel-form network-port-form`}
          {...formItemLayout}
        >
          {getFieldDecorator(`values[${k}]`, {
            rules: [
              {
                required: true,
                message: intl.formatMessage({ id: "required" }),
              },
              {
                validator: this.checkValue,
              },
            ],
          })(
            <Input
              type="text"
              disabled={!getFieldValue("envId")}
              label={<FormattedMessage id="network.config.value" />}
              suffix={<Tips type="form" data="network.label.value.rule" />}
            />
          )}
        </FormItem>
        {targetKeys.length > 1 ? (
          <Icon
            className="network-group-icon"
            type="delete"
            onClick={() => this.removeGroup(k, "targetKeys")}
          />
        ) : null}
      </div>
    ));
    if (this.envSelect && !getFieldValue("envId")) {
      this.envSelect.focus();
    }

    const istOption = ist.length
      ? _.map(
          _.filter(ist, item => item && !_.includes(initIst, item.id)),
          item => {
            const { id, code } = item;
            return (
              <Option key={id} value={id}>
                {code}
              </Option>
            );
          }
        )
      : [];

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          cancelText={<FormattedMessage id="cancel" />}
          okText={<FormattedMessage id="create" />}
          title={<FormattedMessage id="network.header.create" />}
          visible={visible}
          onOk={this.handleSubmit}
          onCancel={this.handleClose.bind(this, false)}
          confirmLoading={submitting}
        >
          <Content
            code="network.create"
            values={{ name: menuName }}
            className="c7n-network-create sidebar-content"
          >
            <Form layout="vertical">
              <FormItem className="c7n-select_512" {...formItemLayout}>
                {getFieldDecorator("envId", {
                  rules: [
                    {
                      required: true,
                      message: intl.formatMessage({ id: "required" }),
                    },
                  ],
                  initialValue: env.length ? this.props.envId : undefined,
                })(
                  <Select
                    ref={this.envSelectRef}
                    className="c7n-select_512"
                    label={<FormattedMessage id="network.env" />}
                    placeholder={intl.formatMessage({ id: "network.env.placeholder" })}
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
                      const { id, connect, name, permission } = item;
                      return (
                        <Option
                          key={id}
                          value={id}
                          disabled={!connect || !permission}
                        >
                          {connect ? (
                            <span className="c7ncd-status c7ncd-status-success" />
                          ) : (
                            <span className="c7ncd-status c7ncd-status-disconnect" />
                          )}
                          {name}
                        </Option>
                      );
                    })}
                  </Select>
                )}
              </FormItem>
              <div
                className={`network-panel-title ${
                  !getFieldValue("envId") ? "network-panel-title_disabled" : ""
                }`}
              >
                <Icon type="instance_outline" />
                <FormattedMessage id="network.target" />
              </div>
              <div className="network-radio-wrap">
                <div
                  className={`network-radio-label ${
                    !getFieldValue("envId")
                      ? "network-radio-label_disabled"
                      : ""
                  }`}
                >
                  <FormattedMessage id="chooseType" />
                </div>
                <FormItem
                  className="c7n-select_512 network-radio-form"
                  label={<FormattedMessage id="chooseType" />}
                  {...formItemLayout}
                >
                  {getFieldDecorator("target", {
                    initialValue: targetType,
                  })(
                    <RadioGroup
                      name="target"
                      disabled={!getFieldValue("envId")}
                      onChange={e => this.handleTypeChange(e, "targetKeys")}
                    >
                      <Radio value="instance">
                        <FormattedMessage id="network.target.instance" />
                      </Radio>
                      <Radio value="param">
                        <FormattedMessage id="network.target.param" />
                      </Radio>
                      <Radio value="endPoints">Endpoints</Radio>
                    </RadioGroup>
                  )}
                </FormItem>
              </div>
              <div className="network-panel">
                {targetType === "instance" && (
                  <Fragment>
                    <FormItem
                      className="c7n-select_480 network-panel-form"
                      {...formItemLayout}
                    >
                      {getFieldDecorator("appId", {
                        rules: [
                          {
                            required: true,
                            message: intl.formatMessage({ id: "required" }),
                          },
                        ],
                        initialValue: this.props.appId
                          ? Number(this.props.appId)
                          : undefined,
                      })(
                        <Select
                          filter
                          showSearch
                          className="c7n-select_480"
                          optionFilterProp="children"
                          disabled={!getFieldValue("envId")}
                          onSelect={this.handleAppSelect}
                          label={<FormattedMessage id="network.form.app" />}
                          getPopupContainer={triggerNode =>
                            triggerNode.parentNode
                          }
                          filterOption={(input, option) =>
                            option.props.children.props.children.props.children.props.name
                              .toLowerCase()
                              .indexOf(input.toLowerCase()) >= 0
                          }
                        >
                          <OptGroup
                            label={<FormattedMessage id="project" />}
                            key="project"
                          >
                            {_.map(localApp, node => this.makeAppGroup(node))}
                          </OptGroup>
                          <OptGroup
                            label={<FormattedMessage id="market" />}
                            key="markert"
                          >
                            {_.map(storeApp, node => this.makeAppGroup(node))}
                          </OptGroup>
                        </Select>
                      )}
                    </FormItem>
                    <FormItem
                      className="c7n-select_480 network-panel-form"
                      {...formItemLayout}
                    >
                      {getFieldDecorator("appInstance", {
                        initialValue: this.state.initIst.length
                          ? this.state.initIst
                          : undefined,
                        trigger: ["onChange", "onSubmit"],
                        rules: [
                          {
                            required: true,
                            message: intl.formatMessage({ id: "required" }),
                          },
                        ],
                      })(
                        <Select
                          filter
                          mode="multiple"
                          className="c7n-select_480 network-select-instance"
                          optionFilterProp="children"
                          optionLabelProp="children"
                          disabled={!getFieldValue("envId")}
                          label={
                            <FormattedMessage id="network.target.instance" />
                          }
                          notFoundContent={intl.formatMessage({
                            id: "network.form.instance.disable",
                          })}
                          getPopupContainer={triggerNode =>
                            triggerNode.parentNode
                          }
                          filterOption={(input, option) =>
                            option.props.children
                              .toLowerCase()
                              .indexOf(input.toLowerCase()) >= 0
                          }
                        >
                          {initIstOption}
                        </Select>
                      )}
                    </FormItem>
                  </Fragment>
                )}
                {targetType === "param" && (
                  <Fragment>
                    {targetItems}
                    <Button
                      disabled={!getFieldValue("envId")}
                      type="primary"
                      funcType="flat"
                      onClick={() => this.addGroup("targetKeys")}
                      icon="add"
                    >
                      <FormattedMessage id="network.config.addtarget" />
                    </Button>
                  </Fragment>
                )}
                {targetType === "endPoints" && (
                  <Fragment>
                    <FormItem
                      className="c7n-select_480 network-panel-form"
                      {...formItemLayout}
                    >
                      {getFieldDecorator("targetIps", {
                        rules: [
                          {
                            required: true,
                            message: intl.formatMessage({ id: "required" }),
                          },
                          {
                            validator: (rule, value, callback) =>
                              this.checkIP(rule, value, callback, "targetIps"),
                          },
                        ],
                      })(
                        <Select
                          mode="tags"
                          ref={node => this.ipSelectRef(node, "targetIps")}
                          disabled={!getFieldValue("envId")}
                          className="c7n-select_512"
                          label={<FormattedMessage id="network.target.ip" />}
                          onInputKeyDown={e =>
                            this.handleInputKeyDown(e, "targetIps")
                          }
                          choiceRender={(liNode, value) =>
                            this.handleChoiceRender(liNode, value, "targetIp")
                          }
                          onChoiceRemove={value =>
                            this.handleChoiceRemove(value, "targetIp")
                          }
                          filterOption={false}
                          notFoundContent={false}
                          showNotFindInputItem={false}
                          showNotFindSelectedItem={false}
                          allowClear
                        />
                      )}
                    </FormItem>
                    {targetPortItems}
                    <Button
                      disabled={!getFieldValue("envId")}
                      type="primary"
                      funcType="flat"
                      onClick={() => this.addGroup("endPoints")}
                      icon="add"
                    >
                      <FormattedMessage id="network.config.addport" />
                    </Button>
                  </Fragment>
                )}
              </div>
              <div
                className={`network-panel-title ${
                  !getFieldValue("envId") ? "network-panel-title_disabled" : ""
                }`}
              >
                <Icon type="router" />
                <FormattedMessage id="network.config" />
              </div>
              <div className="network-radio-wrap">
                <div
                  className={`network-radio-label ${
                    !getFieldValue("envId")
                      ? "network-radio-label_disabled"
                      : ""
                  }`}
                >
                  <FormattedMessage id="chooseType" />
                </div>
                <FormItem
                  className="c7n-select_512 network-radio-form"
                  {...formItemLayout}
                >
                  {getFieldDecorator("config", {
                    initialValue: configType,
                  })(
                    <RadioGroup
                      name="config"
                      disabled={!getFieldValue("envId")}
                      onChange={e => this.handleTypeChange(e, "portKeys")}
                    >
                      <Radio value="ClusterIP">ClusterIP</Radio>
                      <Radio value="NodePort">NodePort</Radio>
                      <Radio value="LoadBalancer">LoadBalancer</Radio>
                    </RadioGroup>
                  )}
                </FormItem>
              </div>
              <div className="network-panel">
                {configType === "ClusterIP" ? (
                  <Fragment>
                    <FormItem
                      className="c7n-select_480 network-panel-form"
                      {...formItemLayout}
                    >
                      {getFieldDecorator("externalIps", {
                        rules: [
                          {
                            validator: this.checkIP,
                          },
                        ],
                      })(
                        <Select
                          mode="tags"
                          ref={this.ipSelectRef}
                          disabled={!getFieldValue("envId")}
                          className="c7n-select_512"
                          label={<FormattedMessage id="network.config.ip" />}
                          onInputKeyDown={this.handleInputKeyDown}
                          choiceRender={this.handleChoiceRender}
                          onChoiceRemove={this.handleChoiceRemove}
                          filterOption={false}
                          notFoundContent={false}
                          showNotFindInputItem={false}
                          showNotFindSelectedItem={false}
                          allowClear
                        />
                      )}
                    </FormItem>
                    {portItems}
                  </Fragment>
                ) : (
                  portItems
                )}
                <Button
                  disabled={!getFieldValue("envId")}
                  type="primary"
                  funcType="flat"
                  onClick={() => this.addGroup("portKeys")}
                  icon="add"
                >
                  <FormattedMessage id="network.config.addport" />
                </Button>
              </div>
              <FormItem
                className="c7n-select_512 network-form-name"
                {...formItemLayout}
              >
                {getFieldDecorator("name", {
                  initialValue: initName,
                  rules: [
                    {
                      required: true,
                      message: intl.formatMessage({ id: "required" }),
                    },
                    {
                      validator: this.checkName,
                    },
                  ],
                })(
                  <Input
                    disabled={!getFieldValue("envId")}
                    maxLength={30}
                    type="text"
                    label={<FormattedMessage id="network.form.name" />}
                  />
                )}
              </FormItem>
            </Form>
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(CreateNetwork)));
