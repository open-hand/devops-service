import React, { Component, Fragment } from 'react/index';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Tooltip, Select } from 'choerodon-ui';
import { Content, Header, Page, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import CiPipelineStore from './stores';
import DevPipelineStore from '../../stores/project/devPipeline';
import DepPipelineEmpty from '../../components/DepPipelineEmpty/DepPipelineEmpty';
import RefreshBtn from '../../components/refreshBtn';
import DevopsStore from '../../stores/DevopsStore';
import { getTableTitle } from '../../utils';
import CiPipelineTable from './CiPipelineTable';

import '../main.scss';
import './index.scss';

const { Option, OptGroup } = Select;
const { AppState } = stores;

@observer
class CiPipelineHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    DevPipelineStore.queryAppData(AppState.currentMenuType.id, 'ci');
  }

  componentWillUnmount() {
    CiPipelineStore.setCiPipelines([]);
    DevopsStore.clearAutoRefresh();
  }

  handleRefresh =(spin = true) => {
    CiPipelineStore.loadPipelines(
      spin,
      DevPipelineStore.selectedApp,
      CiPipelineStore.pagination.current,
      CiPipelineStore.pagination.pageSize,
    );
    DevPipelineStore.queryAppData(AppState.currentMenuType.id);
  };

  handleChange(appId) {
    DevPipelineStore.setSelectApp(appId);
    DevPipelineStore.setRecentApp(appId);
    CiPipelineStore.loadPipelines(true, appId);
  }

  render() {
    const { name } = AppState.currentMenuType;
    const { intl: { formatMessage } } = this.props;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    if (appData && appData.length && appId) {
      DevopsStore.initAutoRefresh('ci', this.handleRefresh);
    }
    const titleName = _.find(appData, ['id', appId]) ? _.find(appData, ['id', appId]).name : name;
    return (
      <Page
        className="c7n-ciPipeline"
        service={[
          'devops-service.application.listByActive',
          'devops-service.project-pipeline.cancel',
          'devops-service.project-pipeline.retry',
          'devops-service.devops-gitlab-pipeline.pagePipeline',
        ]}
      >
        {appData && appData.length && appId ? <Fragment><Header title={<FormattedMessage id="ciPipeline.head" />}>
          <Select
            filter
            className="c7n-header-select"
            dropdownClassName="c7n-header-select_drop"
            placeholder={formatMessage({ id: 'ist.noApp' })}
            value={appData && appData.length ? DevPipelineStore.getSelectApp : undefined}
            disabled={appData.length === 0}
            filterOption={(input, option) => option.props.children.props.children.props.children
              .toLowerCase().indexOf(input.toLowerCase()) >= 0}
            onChange={this.handleChange.bind(this)}
          >
            <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
              {
                _.map(DevPipelineStore.getRecentApp, app => (
                  <Option
                    key={`recent-${app.id}`}
                    value={app.id}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
            <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
              {
                _.map(appData, (app, index) => (
                  <Option
                    value={app.id}
                    key={index}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
          </Select>
          <RefreshBtn name="ci" onFresh={this.handleRefresh} />
        </Header>
        <Content code={appData.length ? 'ciPipeline.app' : 'ciPipeline'} values={{ name: titleName }}>
          <CiPipelineTable store={CiPipelineStore} loading={CiPipelineStore.loading}/>
        </Content></Fragment> : <DepPipelineEmpty title={<FormattedMessage id="ciPipeline.head" />} type="app" />}
      </Page>
    );
  }
}

export default withRouter(injectIntl(CiPipelineHome));
