import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, stores } from '@choerodon/boot';
import _ from 'lodash';
import { Button, Form, Select, Input, Modal, Icon, Table, Popover } from 'choerodon-ui';
import { EditableCell, EditableFormRow } from './editableTable';
import { objToYaml, yamlToObj, takeObject, ConfigNode } from '../utils';
import YamlEditor from '../../../../components/yamlEditor';
import EnvOverviewStore from '../../../../stores/project/envOverview';
import InterceptMask from '../../../../components/interceptMask/InterceptMask';

import '../../../main.scss';
import './KeyValueSideBar.scss';

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { Option } = Select;
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

@Form.create({})
@injectIntl
@inject('AppState')
@observer
export default class KeyValueSideBar extends Component {
  state = {
    // 键值对格式
    dataSource: [new ConfigNode()],
    // yaml 格式
    dataYaml: '',
    counter: 1,
    submitting: false,
    warningDisplay: false,
    warningMes: '',
    data: false,
    isYamlEdit: false,
    hasYamlError: false,
  };

  /**
   * 检查名称唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      store,
      intl: {
        formatMessage,
      },
      form: {
        getFieldValue,
      },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    const envId = getFieldValue('envId');
    if (value && !pattern.test(value)) {
      callback(formatMessage({ id: 'network.name.check.failed' }));
    } else if (value && pattern.test(value)) {
      store.checkName(projectId, envId, value)
        .then((data) => {
          if (data && data.failed) {
            callback(formatMessage({ id: 'template.checkName' }));
          } else {
            callback();
          }
        });
    } else {
      callback();
    }
  }, 1000);

  componentDidMount() {
    const {
      store,
      id,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    EnvOverviewStore.loadActiveEnv(projectId);

    if (typeof id === 'number') {
      store.loadKVById(projectId, id)
        .then((data) => {
          if (data) {

            if (data.failed) {

              Choerodon.prompt(data.message);

            } else {

              let counter = 1;

              if (!_.isEmpty(data.value)) {
                const dataSource = _.map(data.value, (value, key) => new ConfigNode(key, value, counter++));

                this.setState({
                  dataSource,
                  counter,
                });
              }

              this.setState({ data });
            }
          }
        });
    }
  }

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = (value) => {
    const {
      store,
      title,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const loadFnMap = {
      configMap: () => store.loadConfigMap(true, projectId, value),
      secret: () => store.loadSecret(true, projectId, value),
    };

    loadFnMap[title]();
    EnvOverviewStore.setTpEnvId(value);
  };

  /**
   * 删除key-value
   * @param key
   */
  handleDelete = (key) => {
    const dataSource = [...this.state.dataSource].filter(item => item.index !== key);
    this.setState({ dataSource });
  };

  /**
   * 添加一组 key/value
   * @param data
   */
  handleAdd = (data) => {
    const { counter, dataSource } = this.state;

    let _data = data;

    if (!Array.isArray(data)) {
      _data = [[null, null]];
    }

    let _counter = counter;
    let newData = _.map(_data, ([key, value]) => new ConfigNode(key, value, ++_counter));

    if (!newData.length) {
      const initConfig = new ConfigNode();
      newData.push(initConfig);
    }

    const uniqData = _.uniqBy([...dataSource.filter(item => item.index !== ''), ...newData], 'index');
    this.setState({
      dataSource: uniqData,
      counter: _counter,
    });
  };

  /**
   * 保存输入
   * @param row
   */
  handleSave = (row) => {
    const newData = [...this.state.dataSource];
    const index = _.findIndex(newData, ['index', row.index]);

    newData.splice(index, 1, {
      ...newData[index],
      ...row,
    });

    this.setState({ dataSource: newData });
  };

  /**
   * 校验键值对
   * @param data
   * @returns {boolean}
   */
  checkErrorData(data = null) {
    const {
      intl: {
        formatMessage,
      },
    } = this.props;

    const _data = data || this.state.dataSource;
    const hasKey = _data.filter(({ key }) => !_.isEmpty(key));
    const onlyHasValue = _data.filter(({ key, value }) => _.isEmpty(key) && !_.isEmpty(value));
    const onlyHasKey = hasKey.filter(({ value }) => _.isEmpty(value));
    const hasErrorItem = onlyHasKey.length || onlyHasValue.length;
    const hasRepeatKey = hasKey.length !== _.uniqBy(hasKey, 'key').length;

    let hasErrorKey;
    for (const { key } of hasKey) {

      if (/[^0-9A-Za-z\.\-\_]/.test(key)) {
        hasErrorKey = true;
        break;
      }

    }

    if (!(hasErrorItem || hasErrorKey || hasRepeatKey)) return false;

    const errorMsg = formatMessage({
      id: hasRepeatKey ? 'configMap.keyRepeat' : 'configMap.keyValueSpan',
    });

    this.setConfigError(errorMsg);

    return true;
  }

  /**
   * 设置键值对模式下的错误提示
   * @param msg
   */
  setConfigError(msg) {
    this.setState({
      warningMes: msg,
      warningDisplay: true,
      submitting: false,
    });
  }

  /**
   * form提交函数
   * 添加粘贴后key-value校验
   * @param e
   */
  handleSubmit = e => {
    e.preventDefault();
    const {
      form,
      store,
      id,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const {
      dataSource,
      isYamlEdit,
      hasYamlError,
      dataYaml,
    } = this.state;

    let configData = [];
    let hasKVError = false;

    if (!isYamlEdit) {
      hasKVError = this.checkErrorData();
      configData = [...dataSource.filter(item => !_.isEmpty(item.key))];
    } else {
      configData = yamlToObj(dataYaml);
    }

    if (hasYamlError || hasKVError) return;

    this.setState({
      submitting: true,
      warningDisplay: false,
    });

    const uniqData = _.uniqBy(configData, 'index');

    form.validateFieldsAndScroll((err, { name, description, envId }) => {
      if (!err) {

        const _value = takeObject(uniqData);

        const dto = {
          name,
          description,
          envId,
          type: id ? 'update' : 'create',
          id: id || undefined,
          value: _value,
        };

        store.postKV(projectId, dto)
          .then((res) => {
            if (res) {
              if (res && res.failed) {
                this.setState({ submitting: false });
                Choerodon.prompt(res.message);
              } else {
                this.handleClose();
                this.setState({ submitting: false });
              }
            }
          });
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
   * 配置信息的名称描述等常规表单项
   * @returns {*}
   */
  getFormContent = () => {
    const {
      intl: { formatMessage },
      form: { getFieldDecorator },
      envId,
      id,
    } = this.props;
    const { data } = this.state;
    const envData = EnvOverviewStore.getEnvcard;

    return (<Form className="c7n-sidebar-form" layout="vertical">
      <FormItem {...formItemLayout}>
        {getFieldDecorator('envId', {
          initialValue: envData.length ? envId : null,
          rules: [
            {
              required: true,
              message: formatMessage({ id: 'required' }),
            },
          ],
        })(
          <Select
            disabled={!!id}
            className="c7n-select_512"
            label={<FormattedMessage id="ctf.envName" />}
            placeholder={formatMessage({
              id: 'ctf.env.placeholder',
            })}
            optionFilterProp="children"
            onSelect={this.handleEnvSelect}
            filterOption={(input, option) =>
              option.props.children[1]
                .toLowerCase()
                .indexOf(input.toLowerCase()) >= 0
            }
            filter
            showSearch
          >
            {_.map(envData, item => {
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
      <FormItem
        {...formItemLayout}
      >
        {getFieldDecorator('name', {
          initialValue: data ? data.name : null,
          rules: [{
            required: true,
            message: formatMessage({ id: 'required' }),
          }, {
            validator: id ? null : this.checkName,
          }],
        })(
          <Input
            autoFocus={!id}
            disabled={!!id}
            maxLength={100}
            label={<FormattedMessage id="app.name" />}
          />,
        )}
      </FormItem>
      <FormItem
        {...formItemLayout}
      >
        {getFieldDecorator('description', {
          initialValue: data ? data.description : null,
        })(
          <TextArea
            autosize={{ minRows: 2 }}
            maxLength={30}
            label={<FormattedMessage id="configMap.des" />}
          />,
        )}
      </FormItem>
    </Form>);
  };

  /**
   * 编辑 configMap 组件节点
   * 有两种模式：key/value编辑模式、YAML代码编辑模式
   * @returns {*}
   */
  getConfigMap = () => {
    const { title } = this.props;
    const {
      dataSource,
      isYamlEdit,
      warningDisplay,
      warningMes,
      dataYaml,
    } = this.state;

    let configMap = null;
    if (!isYamlEdit) {

      const components = {
        body: {
          row: EditableFormRow,
          cell: EditableCell,
        },
      };
      const baseColumns = [{
        title: 'key',
        dataIndex: 'key',
        width: 230,
        editable: true,
      }, {
        title: '',
        width: 60,
        className: 'icon-equal',
        align: 'center',
        dataIndex: 'temp',
      }, {
        title: title,
        width: 230,
        dataIndex: 'value',
        editable: true,
      }, {
        title: '',
        dataIndex: 'operation',
        render: (text, { index }) => (
          dataSource.length >= 1 ? (
            <Icon
              className="del-btn"
              type="delete"
              onClick={this.handleDelete.bind(this, index)}
            />
          ) : null),
      }];

      const columns = baseColumns.map((col) => {
        if (!col.editable) return col;

        return {
          ...col,
          onCell: record => ({
            record,
            editable: col.editable,
            dataIndex: col.dataIndex,
            title: col.title,
            save: this.handleSave,
            add: this.handleAdd,
          }),
        };
      });

      configMap = <Fragment>
        <Table
          filterBar={false}
          showHeader={false}
          pagination={false}
          components={components}
          className="c7n-editable-table"
          dataSource={dataSource}
          columns={columns}
          rowKey={record => record.index}
        />
        <Button icon="add" onClick={this.handleAdd} type="primary">
          <FormattedMessage id={`${title}.add`} />
        </Button>
        {warningDisplay ? <div className="c7n-cm-warning">{warningMes}</div> : null}
      </Fragment>;

    } else {
      configMap = <YamlEditor
        readOnly={false}
        modeChange={false}
        value={dataYaml}
        onValueChange={this.changeYamlValue}
        handleEnableNext={this.checkYamlError}
      />;
    }

    return configMap;
  };

  /**
   * yaml 值改变
   * @param value
   */
  changeYamlValue = (value) => {
    this.setState({ dataYaml: value });
  };

  /**
   * 校验yaml格式
   * @param flag
   */
  checkYamlError = (flag) => {
    this.setState({ hasYamlError: flag });
  };

  /**
   * 切换配置映射的编辑模式
   */
  changeEditMode = () => {
    const {
      dataSource,
      dataYaml,
      hasYamlError,
    } = this.state;
    const hasError = this.checkErrorData();

    if (hasError || hasYamlError) return;

    if (!this.state.isYamlEdit) {

      const yamlValue = objToYaml(dataSource);

      this.setState({
        warningDisplay: false,
        warningMes: '',
        dataSource: [],
        dataYaml: yamlValue,
        counter: 1,
      });
    } else {

      const kvValue = yamlToObj(dataYaml);
      const counter = kvValue.length;

      this.checkErrorData(kvValue);

      this.setState({
        dataSource: kvValue,
        hasYamlError: false,
        dataYaml: '',
        counter,
      });
    }

    this.setState({
      isYamlEdit: !this.state.isYamlEdit,
    });
  };

  render() {
    const {
      intl: { formatMessage },
      visible,
      id,
      envId,
      title,
    } = this.props;
    const {
      submitting,
      data,
      warningDisplay,
      hasYamlError,
      isYamlEdit,
    } = this.state;

    const envName = (_.find(EnvOverviewStore.getEnvcard, ['id', envId]) || {}).name;
    const titleName = id ? data.name : envName;
    const titleCode = `${title}.${id ? 'edit' : 'create'}`;

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          visible={visible}
          cancelText={<FormattedMessage id="cancel" />}
          okText={<FormattedMessage id={id ? 'save' : 'create'} />}
          title={<FormattedMessage id={titleCode} />}
          onOk={this.handleSubmit}
          onCancel={this.handleClose.bind(this, false)}
          confirmLoading={submitting}
        >
          <Content
            code={titleCode}
            values={{ name: titleName }}
            className="c7n-ctf-create sidebar-content"
          >
            {this.getFormContent()}

            <div className="c7n-sidebar-from-title">
              <FormattedMessage id={`${title}.head`} />
              <Popover
                overlayStyle={{ maxWidth: 350 }}
                content={formatMessage({ id: `${title}.help.tooltip` })}
              >
                <Icon type="help" />
              </Popover>
              <Button
                className="c7n-config-mode-btn"
                type="primary"
                funcType="flat"
                disabled={warningDisplay || hasYamlError}
                onClick={this.changeEditMode}
              >
                <FormattedMessage id={isYamlEdit ? 'configMap.mode.yaml' : 'configMap.mode.kv'} />
              </Button>
            </div>

            <div className="c7n-config-editor">
              {this.getConfigMap()}
            </div>

            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}
