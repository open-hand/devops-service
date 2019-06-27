import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button,
  Tabs,
  Icon,
  Modal,
  Input,
  Table,
  Pagination,
  Checkbox,
} from 'choerodon-ui';
import { Content } from '@choerodon/boot';
import classnames from 'classnames';
import _ from 'lodash';
import LoadingBar from '../../../../components/loadingBar';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import AppListStore from '../../../../stores/project/deployApp/AppListStore';

import '../../../main.scss';
import './AppList.scss';

const CARD_VIEW = 'card';
const LIST_VIEW = 'list';
const TAB_LOCAL = 'local';
const TAB_MARKET = 'market';

const { TabPane } = Tabs;
const ButtonGroup = Button.Group;
const { Sidebar } = Modal;

@injectIntl
@inject('AppState')
@observer
export default class AppList extends Component {
  state = {
    activeTab: TAB_LOCAL,
    view: CARD_VIEW,
    searchValue: '',
    isMarket: false,
  };

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      isMarket,
    } = this.props;
    AppListStore.loadAppsData({ projectId, isMarket });
    this.handleSelectData();
  }

  componentWillUnmount() {
    AppListStore.setLocalData([]);
    AppListStore.setStoreData([]);
    this.clearSearch();
  }

  /**
   * 卡片视图分页器
   */
  onPageChange = (current, pageSize) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { isMarket, searchValue } = this.state;

    AppListStore.loadAppsData({
      projectId,
      isMarket,
      current,
      pageSize,
      postData: { param: searchValue, searchParam: {} },
    });
  };

  /**
   * 初始化选择数据
   */
  handleSelectData = () => {
    const { app, isMarket } = this.props;

    this.setState({
      app,
      isMarket,
      activeTab: isMarket ? TAB_MARKET : TAB_LOCAL,
    });
  };

  /**
   * 卡片视图与列表视图
   * @param view
   */
  changeView = (view) => {
    this.setState({ view });
  };

  /**
   * 卡片选择应用
   * @param record
   */
  handleSelectApp = (record) => {
    this.setState({ app: record });
  };

  /**
   * 表格行选择应用
   * @param record
   */
  handleRowSelectApp = (record) => ({
    onClick: () => this.handleSelectApp(record),
  });

  /**
   * 项目应用与市场应用切换
   * @param key
   */
  changeTab = (key) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const isMarket = key !== TAB_LOCAL;

    this.clearSearch(key);

    this.setState({
      activeTab: key,
      app: undefined,
      isMarket,
    });

    AppListStore.loadAppsData({ projectId, isMarket, current: 1 });
  };

  /**
   * 搜索
   * @param e
   */
  handleSearch = (e) => {
    const { value } = e.target;

    this.setState({ searchValue: value });
    AppListStore.setSearchValue(value);

    // 将e传递给debounce函数时，e.target将会丢失，所以要直接传递值
    this.searchQueryDebounce(value);
  };

  /**
   * 立即发起搜索请求
   * @param e
   */
  searchQueryIm = (e) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { isMarket } = this.state;

    const value = typeof e === 'string' ? e : e.target.value;

    this.setState({ searchValue: value });
    AppListStore.setSearchValue(value);
    AppListStore.loadAppsData({
      projectId,
      isMarket,
      postData: { param: value, searchParam: {} },
      current: 1,
    });
  };

  searchQueryDebounce = _.debounce(this.searchQueryIm, 500);

  /**
   * 清空搜索框数据
   */
  clearSearch = () => {
    AppListStore.setSearchValue('');
    this.setState({ searchValue: '' });
  };

  clearSearchAndLoad = () => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { isMarket } = this.state;

    this.clearSearch();
    AppListStore.loadAppsData({ projectId, isMarket, current: 1 });
  };

  tableChange = ({ current, pageSize }, filters, sorter, paras) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { isMarket } = this.state;

    const postData = {
      searchParam: filters,
      param: paras.toString(),
    };

    AppListStore.loadAppsData({
      projectId,
      isMarket,
      sorter,
      postData,
      current,
      pageSize,
    });
  };

  /**
   * 确定选择数据
   */
  handleOk = () => {
    const { app, activeTab } = this.state;
    const {
      handleOk,
      intl: {
        formatMessage,
      },
    } = this.props;

    const isLocal = activeTab === TAB_LOCAL;

    if (app) {
      handleOk(app, isLocal);
    } else {
      Choerodon.prompt(formatMessage({ id: 'deploy.step.one.app.must' }));
    }
  };

  /**
   * 项目应用
   * @returns {*}
   */
  get getProjectTable() {
    const {
      intl: {
        formatMessage,
      },
    } = this.props;
    const {
      getLocalData,
      getLocalPageInfo,
      getLoading,
    } = AppListStore;
    const { app, isMarket } = this.state;

    const checkCol = ({ id }) => {
      const checked = app && id === app.id && !isMarket;

      return renderTableCheck(checked);
    };

    const column = [{
      key: 'check',
      width: '50px',
      render: checkCol,
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
        filterBarPlaceholder={formatMessage({ id: 'filter' })}
        onRow={this.handleRowSelectApp}
        onChange={this.tableChange}
        columns={column}
        rowKey={record => record.id}
        dataSource={getLocalData}
        loading={getLoading}
        pagination={getLocalPageInfo}
      />
    );
  };

  /**
   * 应用市场应用
   * @returns {*}
   */
  get getMarketTable() {
    const {
      intl: {
        formatMessage,
      },
    } = this.props;
    const {
      getStoreData,
      getStorePageInfo,
      getLoading,
    } = AppListStore;
    const { app, isMarket } = this.state;

    const checkCol = ({ appId }) => {
      const checked = app && appId === app.appId && isMarket;

      return renderTableCheck(checked);
    };

    const column = [{
      key: 'check',
      width: '50px',
      render: checkCol,
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
        onRow={this.handleRowSelectApp}
        filterBarPlaceholder={formatMessage({ id: 'filter' })}
        onChange={this.tableChange}
        columns={column}
        rowKey={record => record.id}
        dataSource={getStoreData}
        loading={getLoading}
        pagination={getStorePageInfo}
      />
    );
  };

  get renderSearch() {
    const {
      intl: { formatMessage },
    } = this.props;
    const {
      searchValue,
    } = this.state;

    const suffix = searchValue
      ? <Icon
        type="close"
        onClick={this.clearSearchAndLoad}
      />
      : null;

    return <div className="c7n-store-search">
      <Input
        value={searchValue}
        prefix={<Icon type="search" />}
        suffix={suffix}
        onChange={this.handleSearch}
        onPressEnter={this.searchQueryIm}
        placeholder={formatMessage({ id: 'deploy.sidebar.search' })}
      />
    </div>;
  }

  get renderProjectCard() {
    const {
      isMarket,
      app,
    } = this.state;
    const {
      getLocalData,
      getLocalPageInfo,
    } = AppListStore;

    const items = getLocalData.length ? _.map(getLocalData, item => {
      const isCurrent = app && app.id === item.id && !isMarket;

      return renderProjectApp(item, isCurrent, this.handleSelectApp);
    }) : <div className="c7n-deploy-noresult"><FormattedMessage id="deploy.app.noresult" /></div>;

    return <Fragment>
      <div>
        {items}
      </div>
      {getLocalData.length ? renderCardPagination({ ...getLocalPageInfo, callback: this.onPageChange }) : null}
    </Fragment>;
  }

  get renderMarketCard() {
    const {
      getStoreData,
      getStorePageInfo,
    } = AppListStore;
    const {
      isMarket,
      app,
    } = this.state;

    const items = getStoreData.length ? _.map(getStoreData, item => {
      const isCurrent = app && app.appId === item.appId && isMarket;

      return renderMarketApp(item, isCurrent, this.handleSelectApp);
    }) : <div className="c7n-deploy-noresult"><FormattedMessage id="deploy.app.noresult" /></div>;

    return <Fragment>
      <div>
        {items}
      </div>
      {getStoreData.length ? renderCardPagination({ ...getStorePageInfo, callback: this.onPageChange }) : null}
    </Fragment>;
  }

  render() {
    const {
      intl: { formatMessage },
      show,
      handleCancel,
      AppState: {
        currentMenuType: {
          name: projectName,
        },
      },
    } = this.props;
    const { view, activeTab } = this.state;

    const loading = AppListStore.getLoading;

    const tabListBtnClass = classnames({
      'c7n-tab-active': view === LIST_VIEW,
    });

    const tabCartBtnClass = classnames({
      'c7n-tab-active': view === CARD_VIEW,
    });

    return (
      <Sidebar
        title={<FormattedMessage id="deploy.step.one.app" />}
        visible={show}
        onOk={this.handleOk}
        okText={formatMessage({ id: 'ok' })}
        cancelText={formatMessage({ id: 'cancel' })}
        onCancel={handleCancel}
      >
        <Content
          className="c7n-deployApp-sidebar sidebar-content"
          code="deploy.sidebar"
          value={projectName}
        >
          <Tabs
            activeKey={activeTab}
            animated={false}
            tabBarExtraContent={(
              <ButtonGroup>
                <Button
                  className={tabListBtnClass}
                  onClick={() => this.changeView(LIST_VIEW)}
                  icon="format_list_bulleted"
                />
                <Button
                  className={tabCartBtnClass}
                  onClick={() => this.changeView(CARD_VIEW)}
                  icon="dashboard"
                />
              </ButtonGroup>
            )}
            onChange={this.changeTab}
          >
            <TabPane
              className="c7n-deploy-tabpane"
              tab={formatMessage({ id: 'deploy.sidebar.project' })}
              key={TAB_LOCAL}
            >
              {view === LIST_VIEW && this.getProjectTable}
              {view === CARD_VIEW && (
                <Fragment>
                  {this.renderSearch}
                  {loading ? <LoadingBar display /> : (this.renderProjectCard)}
                </Fragment>
              )}

            </TabPane>
            <TabPane
              className="c7n-deploy-tabpane"
              tab={formatMessage({ id: 'deploy.sidebar.market' })}
              key={TAB_MARKET}
            >
              {view === LIST_VIEW && this.getMarketTable}
              {view === CARD_VIEW && (
                <Fragment>
                  {this.renderSearch}
                  {loading ? <LoadingBar display /> : (this.renderMarketCard)}
                </Fragment>
              )}
            </TabPane>
          </Tabs>
        </Content>
      </Sidebar>);
  }
}

function renderCardPagination({ total, current, pageSize, callback }) {
  return (<div className="c7n-pagination_right">
    <Pagination
      total={total}
      current={current}
      pageSize={pageSize}
      showSizeChanger
      onChange={callback}
      onShowSizeChange={callback}
    />
  </div>);
}

function renderMarketApp(item, isCurrent, handler) {
  const { id, name, description, category, imgUrl } = item;

  const cardClass = classnames({
    'c7n-store-card': true,
    'c7n-card-active': isCurrent,
  });

  return <div
    key={id}
    role="none"
    className={cardClass}
    onClick={() => handler(item)}
  >
    {isCurrent && <span className="span-icon-check"><Icon type="check" /></span>}
    {imgUrl
      ? <div
        className="c7n-store-card-icon"
        style={{ backgroundImage: `url(${Choerodon.fileServer(imgUrl)})` }}
      />
      : <div className="c7n-store-card-icon" />}
    <div title={name} className="c7n-store-card-name">
      {name}
    </div>
    <div className="c7n-store-card-source">
      {category}
    </div>
    <div title={description} className="c7n-store-card-des-60">
      {description}
    </div>
  </div>;
}

function renderProjectApp(item, isCurrent, handler) {
  const { id, code, name } = item;
  const cardClass = classnames({
    'c7n-store-card': true,
    'c7n-card-active': isCurrent,
  });

  return (
    <div
      key={id}
      role="none"
      className={cardClass}
      onClick={() => handler(item)}
    >
      {isCurrent && <span className="span-icon-check"><Icon type="check" /></span>}
      <div className="c7n-store-card-icon" />
      <div className="c7n-store-card-name">
        <MouserOverWrapper
          text={name}
          width={0.15}
        >
          {name}
        </MouserOverWrapper>
      </div>
      <div title={code} className="c7n-store-card-des-60">
        {code}
      </div>
    </div>
  );
}

function renderTableCheck(checked) {
  return <Checkbox checked={checked} />;
}
