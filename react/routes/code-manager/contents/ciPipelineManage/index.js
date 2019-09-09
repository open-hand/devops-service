import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Content, Page } from '@choerodon/master';
import { injectIntl } from 'react-intl';
import _ from 'lodash';
import CiPipelineStore from './stores';
import DevPipelineStore from '../../stores/DevPipelineStore';
import DevopsStore from '../../stores/DevopsStore';
import CiPipelineTable from './CiPipelineTable.js';
import handleMapStore from '../../main-view/store/handleMapStore';
import Loading from '../../../../components/loading';
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
          'devops-service.pipeline.pageByOptions',
        ]}
      >
        {appData && appData.length && appId ? <Fragment>
          <Content className="c7n-content">
            <CiPipelineTable store={CiPipelineStore} loading={CiPipelineStore.loading} />
          </Content></Fragment> : <Loading display={DevPipelineStore.getLoading} />}
      </Page>
    );
  }
}

export default withRouter(injectIntl(CiPipelineHome));
