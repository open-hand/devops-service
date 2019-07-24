/**
 * @author ale0720@163.com
 * @date 2019-06-11 17:19
 */
import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Select, Icon, Radio, Form, Input } from 'choerodon-ui';
import _ from 'lodash';
import classnames from 'classnames';
import uuidV1 from 'uuid/v1';
import ButtonGroup from '../components/buttonGroup';
import EnvOverviewStore from '../../../stores/project/envOverview';
import { handleCheckerProptError } from '../../../utils';

import './DeployModal.scss';

const { Group: RadioGroup } = Radio;
const { Option } = Select;
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
const MODE_CRATE = 'create';
const MODE_UPDATE = 'update';

@Form.create({})
@injectIntl
@inject('AppState')
@observer
export default class DeployMode extends Component {
  state = {
    mode: MODE_CRATE,
    istNameOk: true,
  };

  /**
   * 检查名字的唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      intl: {
        formatMessage,
      },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      store,
    } = this.props;
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    const { envId } = this.state;

    if (value) {
      if (!pattern.test(value)) {
        this.setState({ istNameOk: false });
        callback(formatMessage({ id: 'network.name.check.failed' }));
        return;
      }

      store
        .checkIstName(projectId, value, envId)
        .then(data => {
          if (data && data.failed) {
            this.setState({ istNameOk: false });
            callback(formatMessage({ id: 'network.name.check.exist' }));
          } else {
            this.setState({ istNameOk: true });
            callback();
          }
        });

    } else {
      this.setState({ istNameOk: false });
      callback();
    }
  }, 800);

  async componentDidMount() {
    const {
      intl: {
        formatMessage,
      },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      store,
    } = this.props;
    const {
      getSelectedApp: app,
      getSelectedVersion: version,
      getSelectedInstance: instance,
      getEnvironment,
    } = store;

    if (!(app && version)) {
      Choerodon.prompt(formatMessage({ id: 'deploy.appOrVersion.empty' }));
      return;
    }

    const response = await EnvOverviewStore.loadActiveEnv(projectId)
      .catch((e) => {
        Choerodon.handleResponseError(e);
      });

    if (handleCheckerProptError(response)) {
      const { getTpEnvId } = EnvOverviewStore;
      const activeEnvs = _.find(response, 'connect');
      const prevEnvId = getEnvironment ? getEnvironment.id : getTpEnvId;
      const currentEnv = _.find(response, { connect: true, id: prevEnvId });
      const envId = (currentEnv || activeEnvs || {}).id;

      const appId = app && app.publishLevel ? app.appId : app.id;
      const istName = getRandomName(app.code);

      this.setState({
        envId,
        istName,
        ...instance,
      });

      if (envId) {
        store.loadInstances(projectId, appId, envId);
      }
    }
  }

  /**
   * 修改实例模式
   * @param e
   */
  handleChangeMode = e => {
    let istName;
    let instanceId;
    let instance = null;
    const { store } = this.props;
    const target = e.target;

    if (target.value === MODE_CRATE) {
      const { getSelectedApp: app } = store;
      const prefix = app ? app.code : '';

      istName = getRandomName(prefix);
    } else {
      const { getInstances } = store;
      const { instanceDto } = this.state;

      if (instanceDto) {
        istName = instanceDto.code;
        instance = instanceDto;
        instanceId = instanceDto.id;
      } else {
        istName = getInstances[0] ? getInstances[0].code : '';
        instance = getInstances[0];
        instanceId = getInstances[0].id;
      }
    }

    this.props.form.setFields({
      name: {
        value: istName,
      },
    });
    this.setState({
      istName,
      instanceId,
      instanceDto: instance,
      istNameOk: true,
      mode: target.value,
    });

    store.clearValue();
  };

  handleSelectInstance = value => {
    const { store } = this.props;
    const instanceDto = _.find(store.getInstances, ['id', value]);
    const istName = (instanceDto || {}).code;

    this.setState({
      instanceId: value,
      instanceDto,
      istName,
    });

    this.props.form.setFields({
      name: {
        value: istName,
      },
    });
    store.clearValue();
  };

  get renderReplaceContent() {
    const {
      store: {
        getInstances,
        getIstLoading,
      },
    } = this.props;
    const { instanceId } = this.state;
    const currentInstanceId = getInstances.length ? getInstances[0].id : undefined;
    const instanceOptions = _.map(getInstances, ({ id, code }) => (
      <Option value={id} key={id}>
        {code}
      </Option>
    ));

    return (<Select
      filter
      loading={getIstLoading}
      className="c7ncd-step-input"
      optionFilterProp="children"
      onSelect={this.handleSelectInstance}
      value={instanceId || currentInstanceId}
      label={<FormattedMessage id="deploy.step.mode.replace.label" />}
      filterOption={(input, option) =>
        option.props.children
          .toLowerCase()
          .indexOf(input.toLowerCase()) >= 0
      }
    >
      {instanceOptions}
    </Select>);
  }

  handleSelectEnv = (value) => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      form: { setFieldsValue },
    } = this.props;
    const { getSelectedApp: app } = store;

    if (!app) return;

    const appId = app.publishLevel ? app.appId : app.id;
    const istName = getRandomName(app.code);

    setFieldsValue({ name: istName });
    this.setState({
      istName,
      envId: value,
      mode: MODE_CRATE,
      instanceId: undefined,
      instanceDto: null,
    });
    // NOTE: 需要改变全局的环境
    EnvOverviewStore.setTpEnvId(value);
    store.loadInstances(projectId, appId, value);
    store.clearValue();
  };

  handleIstNameChange = e => {
    this.setState({
      istNameOk: true,
      istName: e.target.value,
    });
  };

  stepChange() {
    const { store } = this.props;
    const { getEnvcard } = EnvOverviewStore;
    const {
      envId,
      mode,
      istName,
      instanceId,
      instanceDto,
    } = this.state;
    const isChangedVersion = instanceDto && instanceDto.appVersion !== (store.getSelectedVersion || {}).versionId;
    const environment = _.find(getEnvcard, ['id', envId]);
    const selectIst = {
      mode,
      istName,
      instanceId,
      isChangedVersion,
    };

    store.setSelectedInstance(selectIst);
    store.setEnvironment(environment);
  };

  handlePrev = () => {
    const { onChange } = this.props;
    this.stepChange();
    onChange(0);
  };

  handleNext = () => {
    const { onChange } = this.props;
    this.stepChange();
    onChange(2);
  };

  render() {
    const {
      intl: { formatMessage },
      onCancel,
      form: { getFieldDecorator },
      store: {
        getInstances,
      },
    } = this.props;
    const {
      envId,
      mode,
      istName,
      istNameOk,
      instanceId,
    } = this.state;
    const { getEnvcard } = EnvOverviewStore;

    const envOptions = _.map(getEnvcard, ({ id, code, name, connect, permission }) => {
      const envClass = classnames({
        'c7ncd-status': true,
        'c7ncd-status-success': connect,
        'c7ncd-status-disconnect': !connect,
      });

      return <Option
        value={id}
        key={id}
        disabled={!(connect && permission)}
      >
        <span className={envClass} />
        {name}
      </Option>;
    });

    const disabledNext = !envId || !(
      (mode === MODE_CRATE && istName && istNameOk) ||
      (mode === MODE_UPDATE && (instanceId || (getInstances && getInstances.length))));

    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: 'deploy.step.two.description' })}
        </p>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="donut_large" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.two.env.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <Select
              filter
              optionFilterProp="children"
              className="c7ncd-step-input"
              value={envId}
              label={formatMessage({ id: 'deploy.step.two.env' })}
              onSelect={this.handleSelectEnv}
              filterOption={(input, option) =>
                option.props.children[1]
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0
              }
            >
              {envOptions}
            </Select>
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="jsfiddle" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.mode.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <RadioGroup
              onChange={this.handleChangeMode}
              value={mode}
              label={<FormattedMessage id="deploy.step.mode" />}
            >
              <Radio className="c7ncd-deploy-radio" value={MODE_CRATE}>
                <FormattedMessage id="deploy.step.mode.create" />
              </Radio>
              <Radio
                className="c7ncd-deploy-radio"
                value={MODE_UPDATE}
                disabled={!getInstances.length}
              >
                <FormattedMessage id="deploy.step.mode.update" />
                <Icon
                  className="c7ncd-step-item-tip-icon"
                  style={{ verticalAlign: 'text-bottom' }}
                  type="error"
                />
                <span
                  className="c7ncd-step-item-tip-text"
                  style={{ verticalAlign: 'unset' }}
                >
                  {formatMessage({ id: 'deploy.step.mode.help' })}
                </span>
              </Radio>
            </RadioGroup>
            {mode === MODE_UPDATE ? this.renderReplaceContent : null}
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="instance_outline" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.ist.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <Form layout="vertical">
              <FormItem {...formItemLayout}>
                {getFieldDecorator('name', {
                  initialValue: istName,
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                    { validator: this.checkName },
                  ],
                })(
                  <Input
                    className="c7ncd-step-input"
                    onChange={this.handleIstNameChange}
                    disabled={mode !== MODE_CRATE}
                    maxLength={30}
                    label={formatMessage({ id: 'deploy.instance' })}
                    size="default"
                  />,
                )}
              </FormItem>
            </Form>
          </div>
        </div>
        <ButtonGroup
          disabled={disabledNext}
          onNext={this.handleNext}
          onPrev={this.handlePrev}
          onCancel={onCancel}
        />
      </Fragment>
    );
  }
}

function getRandomName(prefix) {
  const randomString = uuidV1();

  return prefix
    ? `${prefix.substring(0, 24)}-${randomString.substring(0, 5)}`
    : randomString.substring(0, 30);
}
