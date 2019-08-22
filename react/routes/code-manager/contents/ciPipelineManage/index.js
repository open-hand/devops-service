import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Tooltip, Select } from 'choerodon-ui';
import { Content, Header, Page, stores } from '@choerodon/master';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import CiPipelineStore from './stores';
import DevPipelineStore from '../../stores/DevPipelineStore';
import DevopsStore from '../../stores/DevopsStore';
import CiPipelineTable from './CiPipelineTable.js';
import handleMapStore from '../../main-view/store/handleMapStore';
import '../../../main.less';
import './index.less';

@injectIntl
@withRouter
@inject('AppState')
@observer
class CiPipelineHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
    handleMapStore.setCodeManagerCiPipelineManage({
      refresh: this.handleRefresh,
      select: this.handleChange,
    });
  }

  componentDidMount() {
    this.handleRefresh();
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
  };

  handleChange(appId) {
    DevPipelineStore.setSelectApp(appId);
    DevPipelineStore.setRecentApp(appId);
    CiPipelineStore.loadPipelines(true, appId);
  }

  render() {
    const { intl: { formatMessage } } = this.props;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
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
        {appData && appData.length && appId ? <Fragment>
          <Content className="c7n-content">
            <CiPipelineTable store={CiPipelineStore} loading={CiPipelineStore.loading} />
          </Content></Fragment> : null}
      </Page>
    );
  }
}

export default withRouter(injectIntl(CiPipelineHome));
