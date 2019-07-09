/**
 * @author ale0720@163.com
 * @date 2019-06-13 15:45
 */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Icon } from 'choerodon-ui';
import _ from 'lodash';
import ButtonGroup from '../components/buttonGroup';
import YamlEditor from '../../../../components/yamlEditor';
import { handleCheckerProptError } from '../../../../utils';

const MODE_UPDATE = 'update';

@withRouter
@injectIntl
@inject('AppState')
@observer
export default class ConfirmInfo extends Component {
  state = {
    loading: false,
  };

  /**
   * 部署应用
   */
  handleDeploy = async () => {
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
        istName,
        mode,
        instanceId,
        isChangedVersion,
      },
      getSelectedValue: {
        templateId,
        configValue,
        isValueChanged,
      },
      getEnvironment,
      getChartValueId,
    } = store;

    if (!(app && version && getEnvironment && istName)) return;

    const appId = app.appId;
    const appVersionId = version.id;
    const appInstanceId = mode === MODE_UPDATE ? instanceId : null;
    // 改变配置信息/替换模式且版本不同
    const isNotChange = !isValueChanged && mode === MODE_UPDATE && !isChangedVersion;

    const deployDTO = {
      appId,
      isNotChange,
      appVersionId,
      appInstanceId,
      type: mode,
      instanceName: istName,
      environmentId: getEnvironment.id,
      values: configValue,
      valueId: templateId || getChartValueId,
    };

    this.setState({ loading: true });
    const response = await store.submitDeployment(projectId, deployDTO)
      .catch((error) => {
        Choerodon.handleResponseError(error);
      });

    this.setState({ loading: false });
    if (handleCheckerProptError(response)) {
      this.returnPrevPage();
    }
  };

  /**
   * 返回到上一个页面
   */
  returnPrevPage = () => {
    const {
      history,
      location: {
        state,
      },
      AppState: {
        currentMenuType: {
          type,
          id,
          organizationId,
          name,
        },
      },
    } = this.props;

    const url = state && state.prevPage && state.prevPage !== 'market' ? `${state.prevPage}-overview` : 'instance';

    history.push(
      `/devops/${url}?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`,
    );
  };

  render() {
    const {
      store: {
        getSelectedApp: app,
        getSelectedVersion: version,
        getSelectedInstance: {
          istName,
          mode,
        },
        getSelectedValue: {
          configValue,
        },
        getEnvironment,
      },
      onCancel,
      onChange,
    } = this.props;
    const { loading } = this.state;

    const appValue = app ? renderValue(app.name, app.code) : null;
    const envValue = getEnvironment ? renderValue(getEnvironment.name, getEnvironment.code) : null;

    const modeValue = <Fragment>
      <FormattedMessage id={`deploy.step.mode.${mode}`} />
      {mode === MODE_UPDATE && <span className="c7ncd-step-info-item-text">({istName})</span>}
    </Fragment>;

    const deployInfo = [
      {
        icon: 'instance_outline',
        label: 'deploy.instance',
        value: istName,
      }, {
        icon: 'widgets',
        label: 'deploy.step.four.app',
        value: appValue,
      }, {
        icon: 'version',
        label: 'deploy.step.four.version',
        value: version ? version.version : null,
      }, {
        icon: 'donut_large',
        label: 'deploy.step.two.env.title',
        value: envValue,
      }, {
        icon: 'jsfiddle',
        label: 'deploy.step.mode',
        value: modeValue,
      }, {
        icon: 'description',
        label: 'deploy.step.config',
        value: null,
      },
    ];

    const infoDom = _.map(deployInfo, ({ icon, label, value }) => (
      <div key={label} className="c7ncd-step-info-item">
        <div className="c7ncd-step-info-item-label">
          <Icon type={icon} className="c7ncd-step-info-item-icon" />
          <FormattedMessage id={label} />：
        </div>
        {value && <div className="c7ncd-step-info-item-value">{value}</div>}
      </div>
    ));

    return (
      <Fragment>
        <div className="c7ncd-step-item c7ncd-step-item-full">
          {infoDom}
          <YamlEditor readOnly value={configValue || ''} />
        </div>
        <ButtonGroup
          nextTextId="deploy.btn.deploy"
          disabled={!(app && version && getEnvironment && mode)}
          loading={loading}
          onNext={this.handleDeploy}
          onPrev={() => onChange(2)}
          onCancel={onCancel}
          primary={['devops-service.application-instance.deploy']}
        />
      </Fragment>
    );
  }
}

function renderValue(name, code) {
  return <Fragment>
    {name}
    <span className="c7ncd-step-info-item-text">({code})</span>
  </Fragment>;
}
