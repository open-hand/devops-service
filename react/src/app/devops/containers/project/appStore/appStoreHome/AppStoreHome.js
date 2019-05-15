import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Input, Icon, Pagination, Table, Popover, Tooltip } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import LoadingBar from '../../../../components/loadingBar';
import './AppStore.scss';
import '../../../main.scss';

const ButtonGroup = Button.Group;

const { AppState } = stores;

@observer
class AppStoreHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
      val: '',
      pageSize: 20,
    };
  }

  componentDidMount() {
    this.loadAppCards();
  }

  componentWillUnmount() {
    const { AppStoreStore } = this.props;
    AppStoreStore.setAppCards([]);
    AppStoreStore.setApp([]);
  }

  /**
   * pageSize 变化的回调
   * @param current 当前页码
   * @param size 每页条数
   */
  onPageSizeChange = (current, size) => {
    const { val } = this.state;
    const pagination = {
      current, pageSize: size,
    };
    this.onChange(pagination, null, null, val);
  };

  /**
   * 页码改变的回调
   * @param page 改变后的页码
   * @param pageSize 每页条数
   */
  onPageChange = (page, pageSize) => {
    const { val } = this.state;
    const pagination = {
      current: page, pageSize,
    };
    this.onChange(pagination, null, null, val);
  };

  /**
   * table 改变的函数
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   * @param param 搜索
   */
  onChange = (pagination, filters, sorter, param) => {
    const { AppStoreStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    const sort = {};
    const searchParam = {};
    const postData = {
      searchParam,
      param: param.toString(),
    };
    this.setState({ pageSize: pagination.pageSize });
    AppStoreStore.loadApps(projectId, pagination.current - 1, pagination.pageSize, sort, postData);
  };

  /**
   * 搜索函数
   */
  onSearch = () => {
    const { pageSize, val } = this.state;
    this.searchInput.focus();
    const pagination = {
      current: 1, pageSize,
    };
    this.onChange(pagination, null, null, val);
  };

  /**
   * 搜索输入赋值
   * @param e
   */
  onChangeSearch = (e) => {
    this.setState({ val: e.target.value });
  };

  /**
   * 刷新函数
   */
  reload = () => {
    this.onSearch();
  };

  /**
   * 跳转导入chart界面
   */
  importChart = () => {
    const projectId = AppState.currentMenuType.id;
    const organizationId = AppState.currentMenuType.organizationId;
    const projectName = AppState.currentMenuType.name;
    const type = AppState.currentMenuType.type;
    this.linkToChange(`/devops/app-market/import?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`);
  };

  /**
   * 跳转导出chart界面
   */
  exportChart = () => {
    const projectId = AppState.currentMenuType.id;
    const organizationId = AppState.currentMenuType.organizationId;
    const projectName = AppState.currentMenuType.name;
    const type = AppState.currentMenuType.type;
    this.linkToChange(`/devops/app-market/export?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`);
  };

  /**
   * 加载应用卡片
   */
  loadAppCards = () => {
    const { AppStoreStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    AppStoreStore.loadApps(projectId);
  };

  /**
   * 跳转应用详情
   * @param id 应用id
   */
  appDetail = (id) => {
    const projectId = AppState.currentMenuType.id;
    const organizationId = AppState.currentMenuType.organizationId;
    const projectName = AppState.currentMenuType.name;
    const type = AppState.currentMenuType.type;
    this.linkToChange(`/devops/app-market/${id}/app?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`);
  };

  /**
   * 处理页面跳转
   * @param url 跳转地址
   */
  linkToChange = (url) => {
    const { history } = this.props;
    history.push(url);
  };

  /**
   * 清除输入
   */
  emitEmpty = () => {
    this.searchInput.focus();
    this.setState({ val: '' });
  };


  listViewChange = (view) => {
    const { AppStoreStore } = this.props;
    AppStoreStore.setListActive(view);
  };


  render() {
    const { type, organizationId, name, id: projectId } = AppState.currentMenuType;
    const {
      AppStoreStore: {
        getPageInfo: {
          total,
          current,
          pageSize,
        },
        getAppCards,
        getListActive,
        isLoading,
      },
      intl: { formatMessage },
    } = this.props;
    const appCards = getAppCards.slice();

    const prefix = <Icon type="search" onClick={this.onSearch} />;
    const suffix = this.state.val ? <Icon type="close" onClick={this.emitEmpty} /> : null;

    const appCardsDom = appCards.length ? _.map(appCards, card => (
      <div
        role="none"
        className="c7n-store-card"
        key={card.id}
        onClick={this.appDetail.bind(this, card.id)}
      >
        {card.imgUrl ? <div className="c7n-store-card-icon" style={{ backgroundImage: `url(${Choerodon.fileServer(card.imgUrl)})` }} />
          : <div className="c7n-store-card-icon" />}
        <div className="c7n-store-card-name">
          <Tooltip title={card.name}>
            {card.name}</Tooltip>
        </div>
        <div className="c7n-store-card-source">
          {card.category}
        </div>
        <div title={card.description} className="c7n-store-card-des">
          {card.description}
        </div>
      </div>)) : (<span className="c7n-none-des">{formatMessage({ id: 'appstore.noReleaseApp' })}</span>);

    const columns = [{
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'name',
      key: 'name',
    }, {
      title: <FormattedMessage id="app.code" />,
      dataIndex: 'code',
      key: 'code',
    }, {
      title: <FormattedMessage id="appstore.category" />,
      dataIndex: 'category',
      key: 'category',
    }, {
      title: <FormattedMessage id="appstore.desc" />,
      dataIndex: 'description',
      key: 'description',
    }, {
      width: 56,
      key: 'action',
      render: record => (
        <Permission
          service={['devops-service.application-market.queryApp']}
          organizationId={organizationId}
          projectId={projectId}
          type={type}
        >
          <Popover placement="bottom" content={<FormattedMessage id="app.appDetail" />}>
            <Button
              size="small"
              shape="circle"
              onClick={this.appDetail.bind(this, record.id)}
            >
              <i className="icon icon-insert_drive_file" />
            </Button>
          </Popover>
        </Permission>
      ),
    }];
    const appListDom = (<Table
      columns={columns}
      dataSource={appCards}
      filterBar={false}
      pagination={false}
      loading={isLoading}
      rowKey={record => record.id}
    />);

    return (
      <Page
        className="c7n-region page-container"
        service={[
          'devops-service.application-market.listAllApp',
          'devops-service.application-market.queryApp',
          'devops-service.application-market.queryAppVersionReadme',
          'devops-service.application-market.exportFile',
          'devops-service.application-market.importApps',
        ]}
      >
        <Header title={<FormattedMessage id="appstore.title" />}>
          <Permission
            type={type}
            organizationId={organizationId}
            service={['devops-service.application-market.importApps']}
          >
            <Button
              icon="get_app"
              funcType="flat"
              onClick={this.importChart}
            >
              <FormattedMessage id="appstore.import" />
            </Button>
          </Permission>
          <Permission
            type={type}
            organizationId={organizationId}
            service={['devops-service.application-market.exportFile']}
          >
            <Button
              icon="file_upload"
              funcType="flat"
              onClick={this.exportChart}
            >
              <FormattedMessage id="appstore.export" />
            </Button>
          </Permission>
          <Button
            icon="refresh"
            funcType="flat"
            onClick={this.reload}
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content code="appstore" values={{ name }}>
          <div className="c7n-store-search">
            <Input
              placeholder={formatMessage({ id: 'appstore.search' })}
              value={this.state.val}
              prefix={prefix}
              suffix={suffix}
              onChange={this.onChangeSearch}
              onPressEnter={this.onSearch}
              // eslint-disable-next-line no-return-assign
              ref={node => this.searchInput = node}
            />
          </div>
          <ButtonGroup>
            <Button icon="format_list_bulleted" onClick={this.listViewChange.bind(this, 'list')} className={getListActive === 'list' ? 'c7n-tab-active' : ''} />
            <Button icon="dashboard" onClick={this.listViewChange.bind(this, 'card')} className={getListActive === 'card' ? 'c7n-tab-active' : ''} />
          </ButtonGroup>
          {isLoading ? <LoadingBar display />
            : (<div className="c7n-store-list-wrap">
              {getListActive === 'card' ? appCardsDom : appListDom}
            </div>)}
          <div className="c7n-store-pagination">
            <Pagination
              total={total}
              current={current}
              pageSize={pageSize}
              showSizeChanger
              onChange={this.onPageChange}
              onShowSizeChange={this.onPageSizeChange}
            />
          </div>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(AppStoreHome));
