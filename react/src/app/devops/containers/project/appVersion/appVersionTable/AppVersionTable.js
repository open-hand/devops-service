import React, { Component, Fragment } from 'react';
import { Table, Tooltip, Button } from 'choerodon-ui';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import TimePopover from '../../../../components/timePopover';
import DevPipelineStore from '../../../../stores/project/devPipeline';
import '../applicationHome/ApplicationVersion.scss';
import '../../../main.scss';

const { AppState } = stores;
@observer
class AppVersionTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  /**
   * 查看环境总览
   */
  linkDeploy = () => {
    const { history } = this.props;
    const {
      id: projectId,
      name: projectName,
      type,
      organizationId,
    } = AppState.currentMenuType;
    history.push({
      pathname: `/devops/env-overview`,
      search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(
        projectName
      )}&organizationId=${organizationId}&overview`,
    });
  };

  tableChange = (pagination) => {
    const { id: projectId } = AppState.currentMenuType;
    const { current, pageSize } = pagination;
    const page = current;
    this.props.store.loadData(projectId, DevPipelineStore.getSelectApp, page, pageSize);
  };

  render() {
    const { store, loading } = this.props;
    const versionData = store.getAllData;

    const columns = [{
      title: <FormattedMessage id="app.appVersion" />,
      dataIndex: 'version',
      key: 'version',
    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'appName',
      key: 'appName',
    }, {
      title: <FormattedMessage id="app.createTime" />,
      dataIndex: 'creationDate',
      key: 'creationDate',
      render: (text, record) => <TimePopover content={record.creationDate} />,
    }, {
      width: 56,
      key: 'link',
      render: () => <Tooltip title={<FormattedMessage id="envoverview.head" />}><Button icon="jsfiddle" shape="circle" onClick={this.linkDeploy} /></Tooltip>,
    }];

    return (<Table
              loading={loading}
              pagination={store.pageInfo}
              columns={columns}
              filterBar={false}
              dataSource={versionData}
              rowKey={record => record.id}
              onChange={this.tableChange}
            />);
  }
}

export default withRouter(injectIntl(AppVersionTable));
