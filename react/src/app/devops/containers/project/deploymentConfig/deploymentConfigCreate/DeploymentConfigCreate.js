import React, { Component, Fragment } from "react";
import { observer, inject } from "mobx-react";
import { injectIntl, FormattedMessage } from "react-intl";
import {
  Button,
  Modal,
  Select,
  Form,
  Input,
  Spin,
} from "choerodon-ui";
import { Content } from "@choerodon/boot";
import _ from "lodash";
import '../../../main.scss';
import './DeploymentConfigCreate.scss';
import YamlEditor from "../../../../components/yamlEditor/YamlEditor";
import InterceptMask from "../../../../components/interceptMask/InterceptMask";

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { TextArea } = Input;
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
@inject('AppState')
@observer
class DeploymentConfigCreate extends Component {
  /**
   * 部署配置名称唯一性校验
   */
  checkName = _.debounce((rule, value, callback) => {
    const p = /^[^ ]+$/;
    const { intl: { formatMessage }, store } = this.props;
    const { singleConfig: { name } } = store;
    if (value && value === name) {
      callback();
      return;
    }
    if (p.test(value)) {
      const {
        store,
        AppState: { currentMenuType: { projectId } },
      } = this.props;
      store.checkName(projectId, value)
        .then(data => {
          if (data && data.failed) {
            callback(formatMessage({ id: "checkNameExist" }));
          } else {
            callback();
          }
        });
    } else {
      callback(formatMessage({ id: "task.name.check.failed" }));
    }
  }, 1000);

  constructor(props) {
    super(props);
    this.state = {
      submitting: false,
      hasEditorError: false,
      changedValue: null,
      appId: null,
    };
  }

  componentDidMount() {
    const {
      id,
      sidebarType,
      store,
      AppState: { currentMenuType: { projectId } },
      state,
      form: { setFieldsValue },
    } = this.props;
    store.loadEnvData(projectId);
    store.loadAppData(projectId);
    const { appId, envId } = state;
    if (appId && envId) {
      this.handleSelect(appId);
      setFieldsValue({ appId });
      setFieldsValue({ envId });
    }
    if (id && sidebarType === 'edit') {
      store.loadDataById(projectId, id)
        .then(data => {
          if (data) {
            this.setState({
              appId: data.appId,
            });
          }
        })
    }
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setValue(null);
    store.setSingleConfig({});
  }

  /**
   * 创建编辑部署配置
   */
  handleSubmit = (e) => {
    e.preventDefault();
    const {
      form,
      store,
      sidebarType,
      id,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const {
      changedValue,
    } = this.state;
    const { getValue, singleConfig: { value, objectVersionNumber } } = store;
    this.setState({ submitting: true });
    form.validateFields((err, data) => {
      if (!err) {
        data.value = changedValue || getValue || value;
        if (sidebarType === 'edit') {
          data.id = id;
          data.objectVersionNumber = objectVersionNumber;
        }
        const promise = store.createData(projectId, data);
        this.handleResponse(promise);
      } else {
        this.setState({ submitting: false });
      }
    })
  };

  /**
   * 处理创建修改请求返回的数据
   * @param promise
   */
  handleResponse = promise => {
    if (promise) {
      promise
        .then(data => {
          this.setState({ submitting: false });
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          } else {
            this.handleClose();
          }
        })
        .catch(err => {
          this.setState({ submitting: false });
          Choerodon.handleResponseError(err);
        });
    }
  };

  /**
   * 关闭侧边栏
   */
  handleClose = (flag = true) => {
    const { onClose } = this.props;
    onClose(flag);
  };

  /**
   * 选择应用
   */
  handleSelect = (value) => {
    const { store } = this.props;
    const { appId } = store.getSingleConfig;
    this.setState({
      appId: value,
      changedValue: null,
      hasEditorError: false,
    });
    if (value === appId) {
      store.setValue(null);
    } else {
      this.loadValue(value);
    }
  };

  /**
   * 加载配置信息
   */
  loadValue = (appId) => {
    const {
      store,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    appId && store.loadValue(projectId, appId);
  };


  /**
   * 获取配置信息
   */
  getYaml = () => {
    const {
      changedValue,
      appId,
    } = this.state;
    const { store: { getValue, singleConfig: { value } } } = this.props;
    return (appId ?
      <YamlEditor
        readOnly={false}
        value={changedValue || getValue || value || ""}
        originValue={getValue || value}
        onValueChange={this.handleChangeValue}
        handleEnableNext={this.handleEnableNext}
      /> : null
    )
  };

  /**
   * 配置信息修改的回调函数
   */
  handleChangeValue = (value) => {
    this.setState({ changedValue: value });
  };

  handleEnableNext = flag => {
    this.setState({ hasEditorError: flag });
  };

  render() {
    const {
      intl: { formatMessage },
      form,
      store,
      sidebarType,
      AppState: { currentMenuType: { name } },
    } = this.props;
    const {
      submitting,
      hasEditorError,
    } = this.state;
    const {
      getFieldDecorator,
    } =form;
    const {
      name: configName,
      description,
      appId,
      envId,
      index,
    } = store.getSingleConfig;
    const appData = store.getAppData;
    const envData = store.getEnvData;
    const loading = store.getValueLoading;
    const env = _.filter(envData, ['id', envId]);

    return (
      <div className="c7n-region">
        <Sidebar
          title={formatMessage({ id: `deploymentConfig.${sidebarType}.header` })}
          visible={sidebarType === 'create' || sidebarType === 'edit'}
          className="c7n-deploymentConfig-create"
          footer={
            [<Button
              key="submit"
              type="primary"
              funcType="raised"
              onClick={this.handleSubmit}
              loading={submitting}
              disabled={hasEditorError || loading}
            >
              {sidebarType === "create"
                ? formatMessage({ id: "create" })
                : formatMessage({ id: "save" })
              }
            </Button>,
            <Button
              key="back"
              funcType="raised"
              onClick={this.handleClose.bind(this, false)}
              disabled={submitting}
              className="c7n-deploymentConfig-sidebar-cancel"
            >
              {formatMessage({ id: "cancel" })}
            </Button>]
          }
        >
          <Content
            code={`deploymentConfig.${sidebarType}`}
            values={{ name: sidebarType === 'create' ? name : configName  }}
            className="sidebar-content"
          >
            <Form layout="vertical">
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator("name", {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: "required" }),
                      whitespace: true,
                    },
                    {
                      validator: this.checkName,
                    },
                  ],
                  initialValue: configName || "",
                })(
                  <Input
                    autoFocus
                    maxLength={30}
                    type="text"
                    label={<FormattedMessage id="deploymentConfig.name" />}
                  />
                )}
              </FormItem>
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator("description", {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: "required" }),
                      whitespace: true,
                    },
                  ],
                  initialValue: description || "",
                })(
                  <TextArea
                    label={<FormattedMessage id="deploymentConfig.des" />}
                    autosize={{ minRows: 2, maxRows: 5 }}
                  />
                )}
              </FormItem>
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator("appId", {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: "required" }),
                    },
                  ],
                  initialValue: appId,
                })(
                  <Select
                    label={formatMessage({ id: "deploy.appName" })}
                    optionFilterProp="children"
                    onChange={ value => this.handleSelect(value)}
                    disabled={sidebarType === "edit" && !index}
                    filter
                    filterOption={(input, option) =>
                      option.props.children
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
                    }
                  >
                    {
                      _.map(appData, ({ id, name }) => (
                        <Option
                          key={id}
                          value={id}
                        >
                          {name}
                        </Option>
                      ))
                    }
                  </Select>
                )}
              </FormItem>
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator("envId", {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: "required" }),
                    },
                  ],
                  initialValue: (env && env.length) ? envId : undefined,
                })(
                  <Select
                    label={formatMessage({ id: "deploy.envName" })}
                    optionFilterProp="children"
                    disabled={sidebarType === "edit" && !index}
                    filter
                    filterOption={(input, option) =>
                      option.props.children[1]
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
                    }
                  >
                    {
                      _.map(envData, ({ id, connect, permission, name }) => (
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
                      ))
                    }
                  </Select>
                )}
              </FormItem>
              <div className="c7n-deploymentConfig-value">
                <FormattedMessage id="deploy.step.config" />
              </div>
              {loading ? <Spin /> : this.getYaml()}
            </Form>
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}

export default DeploymentConfigCreate;
