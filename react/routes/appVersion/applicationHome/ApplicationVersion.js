import React, { Component, Fragment } from 'react/index';
import { Table, Button, Select } from 'choerodon-ui';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Content, Header, Page, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import TimePopover from '../../../components/timePopover';
import './ApplicationVersion.scss';
import '../../main.scss';
import DepPipelineEmpty from '../../../components/DepPipelineEmpty/DepPipelineEmpty';

const { AppState } = stores;
const { Option } = Select;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@observer
class ApplicationVersion extends Component {
  constructor(props) {
    super(props);
    const menu = AppState.currentMenuType;
    this.state = {
      page: 1,
      pageSize: HEIGHT <= 900 ? 10 : 15,
      param: [],
      filters: {},
      postData: { searchParam: {}, param: '' },
      sorter: {
        filed: 'id',
        columnKey: 'id',
        order: 'descend',
      },
      appId: undefined,
    };
  }

  componentDidMount() {
    const { AppVersionStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    AppVersionStore.queryAppData(projectId);
    this.loadAllData();
  }

  /**
   * 选择应用
   * @param e
   */
  handleAppSelect = e => this.setState({
    appId: e,
    param: [],
    filters: {},
    sorter: {
      filed: 'id',
      columnKey: 'id',
      order: 'descend',
    } }, () => this.loadAllData());

  tableChange = (pagination, filters, sorter, paras) => {
    const { current, pageSize } = pagination;
    const page = current;
    const sort = _.isEmpty(sorter) ? {
      filed: 'id',
      columnKey: 'id',
      order: 'descend',
    } : sorter;
    let searchParam = {};
    let param = '';
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    if (paras.length) {
      param = paras.toString();
    }
    const postData = {
      searchParam,
      param,
    };
    this.setState({ page, pageSize, filters, postData, sorter: sort, param: paras });
    this.loadAllData(page, pageSize, sort, postData);
  };

  /**
   * 刷新
   */
  handleRefresh = () => {
    const { page, pageSize, sorter, postData } = this.state;
    this.loadAllData(page, pageSize, sorter, postData);
  };

  loadAllData = (page = 1, sizes = this.state.pageSize, sort = { field: 'id', order: 'descend' }, filter = { searchParam: {}, param: '' }) => {
    const { AppVersionStore } = this.props;
    const { appId } = this.state;
    const { id: projectId } = AppState.currentMenuType;
    AppVersionStore.loadData(projectId, appId, page, sizes, sort, filter);
  };

  render() {
    const { AppVersionStore, intl } = this.props;
    const versionData = AppVersionStore.getAllData;
    const appData = AppVersionStore.getAppData;
    const flag = _.filter(appData, ['permission', true]);
    const { name } = AppState.currentMenuType;
    const {
      param,
      filters,
      sorter: { columnKey, order },
      appId,
    } = this.state;
    const columns = [{
      title: <FormattedMessage id="app.appVersion" />,
      dataIndex: 'version',
      key: 'version',
      sorter: true,
      sortOrder: columnKey === 'version' && order,
      filters: [],
      filteredValue: filters.version || [],
    }, {
      title: <FormattedMessage id="app.code" />,
      dataIndex: 'appCode',
      key: 'appCode',
      sorter: true,
      sortOrder: columnKey === 'appCode' && order,
    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'appName',
      key: 'appName',
      sorter: true,
      sortOrder: columnKey === 'appName' && order,
    }, {
      title: <FormattedMessage id="app.createTime" />,
      dataIndex: 'creationDate',
      key: 'creationDate',
      sorter: true,
      sortOrder: columnKey === 'creationDate' && order,
      render: (text, record) => <TimePopover content={record.creationDate} />,
    }];

    return (
      <Page
        className="c7n-region c7n-appVersion-wrapper"
        service={[
          'devops-service.application.listByActive',
          'devops-service.application-version.pageByOptions',
        ]}
      >
        {flag && flag.length ? <Fragment>
          <Header title={<FormattedMessage id="app.version" />}>
            <Button
              onClick={this.handleRefresh}
            >
              <i className="icon-refresh icon" />
              <FormattedMessage id="refresh" />
            </Button>
          </Header>
          <Content code="appVer" values={{ name }}>
            <Select
              className="c7n-select_512 c7n-appVersion-select"
              value={appId}
              label={this.props.intl.formatMessage({ id: 'chooseApp' })}
              filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
              onChange={this.handleAppSelect}
              allowClear
              filter
            >
              {
                _.map(appData, app =>
                  <Option
                    key={app.id}
                    value={app.id}
                    disabled={!app.permission}
                  >
                    {app.name}
                  </Option>)
              }
            </Select>
            <Table
              filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
              loading={AppVersionStore.loading}
              pagination={AppVersionStore.pageInfo}
              columns={columns}
              filters={param || []}
              dataSource={versionData}
              rowKey={record => record.id}
              onChange={this.tableChange}
            />
          </Content>
        </Fragment> : <DepPipelineEmpty title={<FormattedMessage id="app.version" />} type="app" />}
      </Page>
    );
  }
}

export default withRouter(injectIntl(ApplicationVersion));
