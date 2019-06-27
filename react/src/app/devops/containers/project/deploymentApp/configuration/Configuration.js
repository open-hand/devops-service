/**
 * @author ale0720@163.com
 * @date 2019-06-13 15:11
 */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Select, Icon, Modal, Spin } from 'choerodon-ui';
import _ from 'lodash';
import ButtonGroup from '../components/buttonGroup';
import YamlEditor from '../../../../components/yamlEditor';
import ConfigSidebar from '../components/configSidebar';
import { handlePromptError } from '../../../../utils';

const { Option } = Select;

@withRouter
@injectIntl
@inject('AppState')
@observer
export default class Configuration extends Component {
  state = {
    hasEditorError: false,
    isValueChanged: false,
    displayModal: false,
    shouldDisplayModal: false,
    displayCreateModal: false,
  };

  async componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      location: {
        state,
      },
      AppState: {
        currentMenuType: {
          id,
        },
      },
    } = this.props;
    const {
      getSelectedApp: app,
      getSelectedVersion: version,
      getSelectedInstance: {
        mode,
        instanceId,
      },
      getEnvironment,
      getSelectedValue,
    } = store;
    const { templateId, configValue } = getSelectedValue;
    const isCurrentProjectApp = app && String(app.projectId) === id;
    const isMarketApp = (!isCurrentProjectApp && state && (state.prevPage === 'market' || state.isLocalApp)) || app.publishLevel;

    this.setState({ ...getSelectedValue });

    if (!isMarketApp) {
      store.loadValuesList(projectId, app.appId, getEnvironment.id);
    }

    if (configValue) return;

    if (templateId) {
      store.loadTemplateValue(projectId, templateId);
    } else {
      const response = await store.loadChartValue(projectId, mode, instanceId, version.id);
      if (handlePromptError(response)) {
        response.id && this.selectTemplate(response.id);
      }
    }
  }

  selectTemplate = (value) => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    this.setState({
      templateId: value,
      configValue: undefined,
      isValueChanged: true,
    });

    store.loadTemplateValue(projectId, value);
  };

  clearTemplate = (value) => {
    if (value) return;

    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const {
      getSelectedVersion: version,
      getSelectedInstance: {
        mode,
        instanceId,
      },
    } = store;

    this.setState({
      templateId: undefined,
      configValue: undefined,
      isValueChanged: false,
    });

    store.loadChartValue(projectId, mode, instanceId, version.id);
  };

  /**
   * value 编辑器内容修改
   * @param value
   * @param changed 有效值有无改动
   */
  handleChangeValue = (value, changed = false) => {
    const {
      store: {
        getSelectedApp,
      },
      location: {
        state,
      },
      AppState: {
        currentMenuType: {
          id,
        },
      },
    } = this.props;
    const isCurrentProjectApp = getSelectedApp && String(getSelectedApp.projectId) === id;
    const isMarketApp = (!isCurrentProjectApp && state && (state.prevPage === 'market' || state.isLocalApp)) || getSelectedApp.publishLevel;

    this.setState({
      configValue: value,
      isValueChanged: changed,
      shouldDisplayModal: !isMarketApp && changed,
    });
  };

  handleYamlCheck = (flag) => {
    this.setState({ hasEditorError: flag });
  };

  /**
   * 离开该步骤前的数据存储
   */
  stepChange() {
    const { store } = this.props;
    const configValue = this.state.configValue || store.getCurrentValue;
    const selected = _.pick(this.state, ['isValueChanged', 'templateId']);

    store.setSelectedValue({
      ...selected,
      configValue,
    });
  }

  /**
   * 下一步前判断是否对模版进行操作
   * （无论是否选择模版）不修改value值不需要修改template
   * 修改了默认值则需要进行询问
   */
  handleNext = () => {
    const { shouldDisplayModal } = this.state;

    if (shouldDisplayModal) {
      this.setState({
        displayModal: shouldDisplayModal,
      });
    } else {
      this.stepToNext();
    }
  };

  handlePrev = () => {
    const { onChange } = this.props;
    this.stepChange();
    onChange(1);
  };

  stepToNext = () => {
    const { onChange } = this.props;
    this.stepChange();
    onChange(3);
  };

  /**
   * 创建新的部属配置
   */
  handleCreateTemplate = () => {
    this.setState({
      displayCreateModal: true,
      displayModal: false,
    });
  };

  handleUpdateTemplate = async () => {
    const {
      intl: { formatMessage },
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { getConfigList } = store;
    const { templateId, configValue } = this.state;
    const config = _.find(getConfigList, ['id', templateId]);

    if (config) {
      const items = _.pick(config, ['appId', 'description', 'id', 'name', 'objectVersionNumber']);
      const data = {
        ...items,
        value: configValue,
      };

      this.setState({ modalLoading: true });

      const response = await store.changeConfig(projectId, data)
        .catch((error) => {
          Choerodon.handleResponseError(error);
        });

      if (handlePromptError(response)) {
        this.setState({
          shouldDisplayModal: false,
          displayModal: false,
        });
        Choerodon.prompt(formatMessage({ id: 'deploy.config.update.success' }));
        this.setState({ modalLoading: false });

        store.loadTemplateValue(projectId, templateId);
        this.stepToNext();
      }
    } else {
      Choerodon.prompt(formatMessage({ id: 'deploy.config.update.failed' }));
    }
  };

  handleCancelCreate = () => {
    this.setState({
      displayCreateModal: false,
    });
  };

  /**
   *
   * @param id 新创建的部属配置的id
   */
  afterCreate = (id) => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const {
      getSelectedApp: app,
      getEnvironment,
    } = store;

    this.setState({
      displayCreateModal: false,
      shouldDisplayModal: false,
      templateId: id,
      configValue: undefined,
    });

    store.loadValuesList(projectId, app.appId, getEnvironment.id);
    store.loadTemplateValue(projectId, id);
  };

  get renderModal() {
    const {
      templateId,
      displayModal,
      modalLoading,
    } = this.state;

    const mode = templateId ? 'update' : 'create';

    return <Modal
      visible={displayModal}
      cancelText={<FormattedMessage id={`deploy.config.${mode}.cancel`} />}
      title={<FormattedMessage id={`deploy.config.${mode}.title`} />}
      okText={<FormattedMessage id={`deploy.config.${mode}.submit`} />}
      closable={false}
      onOk={mode === 'create' ? this.handleCreateTemplate : this.handleUpdateTemplate}
      onCancel={this.stepToNext}
      confirmLoading={modalLoading}
    >
      <div className="c7n-padding-top_8">
        <FormattedMessage id={`deploy.config.${mode}.describe`} />
      </div>
    </Modal>;
  }

  render() {
    const {
      intl: { formatMessage },
      store,
      onCancel,
      location: {
        state,
      },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const {
      configValue,
      hasEditorError,
      templateId,
      displayModal,
      displayCreateModal,
    } = this.state;

    const {
      getConfigList,
      getCurrentValue,
      getConfigLoading,
      getValueLoading,
      getSelectedApp: app,
      getEnvironment,
    } = store;

    const configOptions = _.map(getConfigList, ({ id, name }) => (
      <Option value={id} key={id}>
        {name}
      </Option>
    ));

    const enableClick = !(configValue || getCurrentValue) || hasEditorError;
    const isCurrentProjectApp = app && String(app.projectId) === projectId;
    const disableSelectConfig = (!isCurrentProjectApp && state && (state.prevPage === 'market' || state.isLocalApp)) || !!app.publishLevel;

    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: 'deploy.step.three.description' })}
        </p>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="description" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.config' })}
            </span>
            <Icon className="c7ncd-step-item-tip-icon" type="error" />
            <span className="c7ncd-step-item-tip-text">
              {formatMessage({ id: 'deploy.step.config.description' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <Select
              filter
              allowClear
              disabled={disableSelectConfig}
              className="c7ncd-step-input"
              optionFilterProp="children"
              loading={getConfigLoading}
              value={templateId}
              label={formatMessage({ id: 'deploy.step.config.template' })}
              onSelect={this.selectTemplate}
              onChange={this.clearTemplate}
              filterOption={(input, option) => option.props.children
                .toLowerCase()
                .indexOf(input.toLowerCase()) >= 0
              }
            >
              {configOptions}
            </Select>
          </div>
          <div className="c7ncd-step-indent">
            <Spin spinning={getValueLoading}>
              <YamlEditor
                readOnly={false}
                value={configValue || getCurrentValue}
                originValue={getCurrentValue}
                onValueChange={this.handleChangeValue}
                handleEnableNext={this.handleYamlCheck}
              />
            </Spin>
          </div>
        </div>
        <ButtonGroup
          disabled={enableClick}
          onNext={this.handleNext}
          onPrev={this.handlePrev}
          onCancel={onCancel}
        />
        {displayModal && this.renderModal}
        {displayCreateModal && <ConfigSidebar
          store={store}
          visible={displayCreateModal}
          app={app}
          env={getEnvironment}
          value={configValue}
          onOk={this.afterCreate}
          onCancel={this.handleCancelCreate}
        />}
      </Fragment>
    );
  }
}
