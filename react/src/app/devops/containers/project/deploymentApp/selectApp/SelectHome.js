import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button, Tabs, Icon, Modal, Input, Table, Pagination,
} from 'choerodon-ui';
import { stores, Content } from '@choerodon/boot';
import Loadingbar from '../../../../components/loadingBar';
import '../../../main.scss';
import './SelectApp.scss';
import SelectAppStore from '../../../../stores/project/deploymentApp/SelectAppStore';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';

const { TabPane } = Tabs;
const ButtonGroup = Button.Group;
const SideBar = Modal.Sidebar;
const { AppState } = stores;

@observer
class DeployAppHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
      activeTab: '1',
      projectId: AppState.currentMenuType.id,
      view: 'card',
    };
  }

  componentDidMount() {
    const { projectId } = this.state;
    SelectAppStore.loadData({ projectId });
    this.handleSelectData();
  }

  componentWillUnmount() {
    SelectAppStore.setAllData([]);
    SelectAppStore.setStoreData([]);
    this.clearInputValue();
  }

  /**
   * 切换分页
   * @param page
   * @param size
   */
  onPageChange =(page, size) => {
    const { activeTab, projectId } = this.state;
    if (activeTab === '1') {
      SelectAppStore.loadData({
        projectId,
        page: page - 1,
        size,
      });
    } else {
      SelectAppStore.loadApps({
        projectId,
        page: page - 1,
        size,
      });
    }
  };

  /**
   * 获取本项目的app
   * @returns {*}
   */
  getProjectTable = () => {
    const { intl } = this.props;
    const { app, isMarket } = this.state;
    const dataSource = SelectAppStore.getAllData;
    const column = [{
      key: 'check',
      width: '50px',
      render: record => (
        app && record.id === app.id && !isMarket && <i className="icon icon-check icon-select" />
      ),

    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      filters: [],
    }, {
      title: <FormattedMessage id="app.code" />,
      dataIndex: 'code',
      key: 'code',
      sorter: true,
      filters: [],
    }];
    return (
      <Table
        filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
        rowClassName="col-check"
        onRow={(record) => {
          const a = record;
          return {
            onClick: this.handleSelectApp.bind(this, record),
          };
        }}
        onChange={this.tableChange}
        columns={column}
        rowKey={record => record.id}
        dataSource={dataSource}
        pagination={SelectAppStore.getLocalPageInfo}
      />
    );
  };

  /**
   * 获取应用市场的数据
   * @returns {*}
   */
  getMarketTable = () => {
    const { intl } = this.props;
    const { app, isMarket } = this.state;
    const dataSource = SelectAppStore.getStoreData;
    const column = [{
      key: 'check',
      width: '50px',
      render: record => (
        app && isMarket && record.appId === app.appId && <i className="icon icon-check icon-select" />
      ),

    }, {
      title: <FormattedMessage id="appstore.name" />,
      dataIndex: 'name',
      key: 'name',
    }, {
      title: <FormattedMessage id="appstore.contributor" />,
      dataIndex: 'contributor',
      key: 'contributor',
    }, {
      title: <FormattedMessage id="appstore.category" />,
      dataIndex: 'category',
      key: 'category',
    }, {
      title: <FormattedMessage id="appstore.description.label" />,
      dataIndex: 'description',
      key: 'description',
    }];
    return (
      <Table
        onRow={(record) => {
          const a = record;
          return {
            onClick: this.handleSelectApp.bind(this, record),
          };
        }}
        filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
        rowClassName="col-check"
        onChange={this.tableChange}
        columns={column}
        rowKey={record => record.id}
        dataSource={dataSource}
        pagination={SelectAppStore.getStorePageInfo}
      />
    );
  };

  /**
   * 初始化选择数据
   */
  handleSelectData =() => {
    if (this.props.app) {
      if (this.props.isMarket) {
        const app = this.props.app;
        app.appId = app.id;
      }
      this.setState({ app: this.props.app, isMarket: this.props.isMarket });
    }
  };

  /**
   * 切换视图
   * @param view
   */
  changeView =(view) => {
    this.setState({ view });
  };

  /**
   * 搜索
   * @param e
   */
  handleSearch =(e) => {
    const { activeTab, projectId } = this.state;
    this.setState({ val: e.target.value });
    SelectAppStore.setSearchValue(e.target.value);
    if (activeTab === '1') {
      SelectAppStore.loadData({
        projectId,
        postData: { param: e.target.value, searchParam: {} },
        page: 0,
      });
    } else {
      SelectAppStore.loadApps({
        projectId,
        postData: { param: e.target.value, searchParam: {}, page: 0 },
      });
    }
  };

  /**
   * 清空搜索框数据
   */
  clearInputValue = (key) => {
    const { projectId, activeKey } = this.state;
    const keys = key || activeKey;
    SelectAppStore.setSearchValue('');
    this.setState({ val: '' });
    if (keys === '1') {
      SelectAppStore.loadData({
        projectId,
        page: 0,
        size: SelectAppStore.localPageInfo.pageSize,
      });
    } else {
      SelectAppStore.loadApps({
        projectId,
        page: 0,
        size: SelectAppStore.storePageInfo.pageSize,
      });
    }
  };

  /**
   * 点击选择数据
   * @param record
   */
  handleSelectApp = (record) => {
    const { activeTab } = this.state;
    this.setState({ app: record, isMarket: activeTab === '2' });
  };

  /**
   * table 改变的函数
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   */
  tableChange =(pagination, filters, sorter, paras) => {
    const { activeTab } = this.state;
    const menu = AppState.currentMenuType;
    const organizationId = menu.id;
    const sort = { field: 'id', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      // sort = sorter;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let searchParam = {};
    const page = pagination.current - 1;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    if (activeTab === '1') {
      SelectAppStore.loadData({
        projectId: organizationId,
        sort,
        postData,
        page,
        size: pagination.pageSize,
      });
    } else {
      SelectAppStore.loadApps({
        projectId: organizationId,
        sort,
        postData,
        page,
        size: pagination.pageSize,
      });
    }
  };

  /**
   * 切换tabs
   * @param key
   */
  changeTab =(key) => {
    this.clearInputValue(key);
    this.setState({
      activeTab: key,
      app: undefined,
    });
  };

  /**
   * 确定选择数据
   */
  handleOk =() => {
    const { app, activeTab } = this.state;
    const { handleOk, intl } = this.props;
    if (app) {
      handleOk(app, activeTab);
    } else {
      Choerodon.prompt(intl.formatMessage({ id: 'network.form.version.disable' }));
    }
  };

  render() {
    const { intl: { formatMessage }, show, handleCancel } = this.props;
    const {
      val, view, isMarket, app, activeTab,
    } = this.state;
    const localDataSource = SelectAppStore.getAllData;
    const storeDataSource = SelectAppStore.getStoreData;
    const { total: lt, current: lc, pageSize: lp } = SelectAppStore.getLocalPageInfo;
    const { total: st, current: sc, pageSize: sp } = SelectAppStore.getStorePageInfo;
    const projectName = AppState.currentMenuType.name;
    const prefix = <Icon type="search" onClick={this.handleSearch} />;
    const suffix = val ? <Icon type="close" onClick={() => this.clearInputValue(activeTab)} /> : null;
    const loading = SelectAppStore.getLoading;
    return (
      <SideBar
        title={<FormattedMessage id="deploy.step.one.app" />}
        visible={show}
        onOk={this.handleOk}
        okText={formatMessage({ id: 'ok' })}
        cancelText={formatMessage({ id: 'cancel' })}
        onCancel={handleCancel}
      >
        <Content className="c7n-deployApp-sidebar sidebar-content" code="deploy.sidebar" value={projectName}>
          <div>
            <Tabs
              animated={false}
              tabBarExtraContent={(
                <ButtonGroup>
                  <Button onClick={this.changeView.bind(this, 'list')} className={view === 'list' ? 'c7n-tab-active' : ''}><Icon type="format_list_bulleted" /></Button>
                  <Button onClick={this.changeView.bind(this, 'card')} className={view === 'card' ? 'c7n-tab-active' : ''}><Icon type="dashboard" /></Button>
                </ButtonGroup>
              )}
              onChange={this.changeTab}

            >
              <TabPane className="c7n-deploy-tabpane" tab={formatMessage({ id: 'deploy.sidebar.project' })} key="1">
                {view === 'list' && this.getProjectTable()}
                {view === 'card' && (
                  <React.Fragment>
                    <div className="c7n-store-search">
                      <Input
                        value={val}
                        prefix={prefix}
                        suffix={suffix}
                        onChange={this.handleSearch}
                        onPressEnter={this.handleSearch}
                        placeholder={formatMessage({ id: 'deploy.sidebar.search' })}
                        // eslint-disable-next-line no-return-assign
                        ref={node => this.searchInput = node}
                      />
                    </div>
                    {loading ? <Loadingbar display /> : (
                      <React.Fragment>
                        <div>
                          {localDataSource.length >= 1 && localDataSource.map(card => (
                            <div
                              key={card.id}
                              role="none"
                              className={`c7n-store-card ${app && app.id === card.id && !isMarket ? 'c7n-card-active' : ''}`}
                              onClick={this.handleSelectApp.bind(this, card)}
                            >
                              {app && !isMarket && app.id === card.id && <span className="span-icon-check"><i className="icon icon-check" /></span> }
                              <div className="c7n-store-card-icon" />
                              <div className="c7n-store-card-name">
                                <MouserOverWrapper
                                  text={card.name}
                                  width={0.15}
                                >
                                  {card.name}
                                </MouserOverWrapper>
                              </div>
                              <div title={card.code} className="c7n-store-card-des-60">
                                {card.code}
                              </div>
                            </div>
                          ))}
                        </div>
                        <div className="c7n-store-pagination">
                          <Pagination
                            total={lt}
                            current={lc}
                            pageSize={lp}
                            showSizeChanger
                            onChange={this.onPageChange}
                            onShowSizeChange={this.onPageChange}
                          />
                        </div>
                      </React.Fragment>
                    )}
                  </React.Fragment>
                )}

              </TabPane>
              <TabPane className="c7n-deploy-tabpane" tab={formatMessage({ id: 'deploy.sidebar.market' })} key="2">
                {view === 'list' && this.getMarketTable()}
                {view === 'card' && (
                  <React.Fragment>
                    <div className="c7n-store-search">
                      <Input
                        placeholder={formatMessage({ id: 'deploy.sidebar.search' })}
                        value={val}
                        prefix={prefix}
                        suffix={suffix}
                        // onChange={e => _.debounce(() => this.handleSearch(e), 1000)}
                        onChange={this.handleSearch}
                        onPressEnter={this.handleSearch}
                        // eslint-disable-next-line no-return-assign
                        ref={node => this.searchInput = node}
                      />
                    </div>
                    {loading ? <Loadingbar display /> : (
                      <React.Fragment>
                        <div>
                          {storeDataSource.length >= 1 && storeDataSource.map(card => (
                            <div
                              key={card.id}
                              role="none"
                              className={`c7n-store-card ${app && isMarket && app.appId === card.appId ? 'c7n-card-active' : ''}`}
                              onClick={this.handleSelectApp.bind(this, card)}
                            >
                              {app && app.appId === card.appId && isMarket && <span className="span-icon-check"><i className="icon icon-check " /></span> }
                              {card.imgUrl ? <div className="c7n-store-card-icon" style={{ backgroundImage: `url(${Choerodon.fileServer(card.imgUrl)})` }} />
                                : <div className="c7n-store-card-icon" />}
                              <div title={card.name} className="c7n-store-card-name">
                                {card.name}
                              </div>
                              <div className="c7n-store-card-source">
                                {card.category}
                              </div>
                              <div title={card.description} className="c7n-store-card-des-60">
                                {card.description}
                              </div>
                            </div>
                          ))}
                        </div>
                        <div className="c7n-store-pagination">
                          <Pagination
                            total={st}
                            current={sc}
                            pageSize={sp}
                            showSizeChanger
                            onChange={this.onPageChange}
                            onShowSizeChange={this.onPageChange}
                          />
                        </div>
                      </React.Fragment>
                    )}
                  </React.Fragment>
                )}
              </TabPane>
            </Tabs>
          </div>
        </Content>
      </SideBar>);
  }
}

export default withRouter(injectIntl(DeployAppHome));
