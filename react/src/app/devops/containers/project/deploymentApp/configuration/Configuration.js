/**
 * @author ale0720@163.com
 * @date 2019-06-13 15:11
 */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { Select, Icon } from 'choerodon-ui';
import _ from 'lodash';
import ButtonGroup from '../components/buttonGroup';
import YamlEditor from '../../../../components/yamlEditor';

const { Option } = Select;

@injectIntl
@inject('AppState')
@observer
export default class Configuration extends Component {
  state = {
    hasEditorError: false,
    isValueChanged: false,
  };

  componentDidMount() {
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
      getSelectedVersion: version,
      getSelectedInstance: {
        mode,
        instanceId,
      },
      getEnvironment,
      getSelectedValue,
    } = store;

    this.setState({ ...getSelectedValue });

    store.loadValuesList(projectId, app.appId, getEnvironment.id);
    store.loadValue(projectId, mode, instanceId, version.id);
  }

  selectTemplate = (value) => {
    const { store } = this.props;
    const current = _.find(store.getConfigList, ['id', value]);

    store.setConfigValue(null);
    this.setState({
      templateId: value,
      configValue: undefined,
      isValueChanged: true,
    });

    setTimeout(() => store.setConfigValue({ yaml: current.value }), 0);
  };

  /**
   * value 编辑器内容修改
   * @param value
   * @param changed 有效值有无改动
   */
  handleChangeValue = (value, changed = false) => {
    this.setState({ configValue: value, isValueChanged: changed });
  };

  handleSecondNextStepEnable = flag => {
    this.setState({ hasEditorError: flag });
  };

  stepChange() {
    const { store } = this.props;

    store.setSelectedValue({
      ...this.state,
    });
  }

  handleNext = () => {
    const { onChange } = this.props;
    this.stepChange();
    onChange(3);
  };

  handlePrev = () => {
    const { onChange } = this.props;
    this.stepChange();
    onChange(1);
  };

  render() {
    const {
      intl: { formatMessage },
      store: {
        getConfigList,
        getConfigValue,
        getConfigLoading,
      },
      onCancel,
    } = this.props;
    const {
      configValue,
      hasEditorError,
      templateId,
    } = this.state;

    const configOptions = _.map(getConfigList, ({ id, name }) => (
      <Option value={id} key={id}>
        {name}
      </Option>
    ));

    const initValue = getConfigValue ? getConfigValue.yaml : '';
    const enableClick = !(configValue || initValue) || hasEditorError;

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
              className="c7ncd-step-input"
              optionFilterProp="children"
              loading={getConfigLoading}
              value={templateId}
              label={formatMessage({ id: 'deploy.step.config.template' })}
              onSelect={this.selectTemplate}
              filterOption={(input, option) =>
                option.props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0
              }
            >
              {configOptions}
            </Select>
          </div>
          <div className="c7ncd-step-indent">
            {getConfigValue && <YamlEditor
              readOnly={false}
              value={configValue || initValue}
              originValue={initValue}
              onValueChange={this.handleChangeValue}
              handleEnableNext={this.handleSecondNextStepEnable}
            />}
          </div>
        </div>
        <ButtonGroup
          disabled={enableClick}
          onNext={this.handleNext}
          onPrev={this.handlePrev}
          onCancel={onCancel}
        />
      </Fragment>
    );
  }
}
