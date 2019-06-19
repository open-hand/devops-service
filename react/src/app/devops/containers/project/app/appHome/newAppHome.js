/**
 * @author ale0720@163.com
 * @date 2019-05-23 16:33
 */
import React, { Component, Fragment } from 'react';
import {
  Table,
  Button,
  Input,
  Form,
  Modal,
  Tooltip,
  Select,
  Icon,
  Popover,
  Radio,
  Tag,
} from 'choerodon-ui';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from '@choerodon/boot';
import _ from 'lodash';
import { injectIntl, FormattedMessage } from 'react-intl';
import { commonComponent } from '../../../../components/commonFunction';
import LoadingBar from '../../../../components/loadingBar';
import RefreshBtn from '../../../../components/refreshBtn';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import Tips from '../../../../components/Tips/Tips';
import InterceptMask from '../../../../components/interceptMask/InterceptMask';

import '../../envPipeline/EnvPipeLineHome.scss';
import './AppHome.scss';
import '../../../main.scss';
import '../index.scss';

const { AppState } = stores;
const { Sidebar } = Modal;
const { Option } = Select;
const RadioGroup = Radio.Group;
const FormItem = Form.Item;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

@commonComponent('AppStore')
@observer
class AppHome extends Component {
  constructor(props) {
    const menu = AppState.currentMenuType;
    const {
      location: { state },
    } = props.history;
    super(props);
    this.state = {
      page: 1,
      id: '',
      projectId: menu.id,
      show: state && state.show,
      type: state && state.modeType,
      submitting: false,
      checked: true,
      selected: [],
      createSelectedRowKeys: [],
      createSelected: [],
      createSelectedTemp: [],
    };
  }

  componentDidMount() {
    const {
      location: { state },
    } = this.props;
    const { show, modeType } = state || {};
    show && modeType && this.showSideBar('create');
    this.loadAllData(0);
  }

  componentWillUnmount() {
    this.clearAutoRefresh();
    this.clearFilterInfo();
  }

  getColumn = () => {
    const {
      AppStore,
      intl: { formatMessage },
    } = this.props;
    const {
      type,
      id: projectId,
      organizationId: orgId,
    } = AppState.currentMenuType;
    const {
      filters,
      sort: { columnKey, order },
    } = AppStore.getInfo;
    return [
      {
        title: <FormattedMessage id="app.type" />,
        dataIndex: 'type',
        key: 'type',
        filters: [
          {
            text: formatMessage({ id: 'app.type.normal' }),
            value: '0',
          },
          {
            text: formatMessage({ id: 'app.type.test' }),
            value: '1',
          },
        ],
        filteredValue: filters.type || [],
        render: text =>
          text ? <FormattedMessage id={`app.type.${text}`} /> : '',
      },
      {
        title: <FormattedMessage id="app.name" />,
        dataIndex: 'name',
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: text => (
          <MouserOverWrapper text={text} width={0.2}>
            {text}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="app.code" />,
        dataIndex: 'code',
        key: 'code',
        sorter: true,
        sortOrder: columnKey === 'code' && order,
        filters: [],
        filteredValue: filters.code || [],
        render: text => (
          <MouserOverWrapper text={text} width={0.25}>
            {text}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="app.url" />,
        dataIndex: 'repoUrl',
        key: 'repoUrl',
        render: text => (
          <MouserOverWrapper text={text} width={0.25}>
            <a
              href={text}
              rel="nofollow me noopener noreferrer"
              target="_blank"
            >
              {text ? `../${text.split('/')[text.split('/').length - 1]}` : ''}
            </a>
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="app.active" />,
        dataIndex: 'active',
        key: 'active',
        filters: [
          {
            text: formatMessage({ id: 'app.stop' }),
            value: '0',
          },
          {
            text: formatMessage({ id: 'app.run' }),
            value: '1',
          },
          {
            text: formatMessage({ id: 'app.failed' }),
            value: '-1',
          },
          {
            text: formatMessage({ id: 'app.creating' }),
            value: '2',
          },
        ],
        filteredValue: filters.active || [],
        render: this.getAppStatus,
      },
      {
        align: 'right',
        width: 104,
        key: 'action',
        render: record => (
          <Fragment>
            {record.sonarUrl ? (
              <Tooltip
                title={<FormattedMessage id="app.quality" />}
                placement="bottom"
              >
                <a
                  href={record.sonarUrl}
                  rel="nofollow me noopener noreferrer"
                  target="_blank"
                >
                  <Button icon="quality" shape="circle" size="small" />
                </a>
              </Tooltip>
            ) : null}
            {!record.fail && (
              <Fragment>
                <Permission
                  type={type}
                  projectId={projectId}
                  organizationId={orgId}
                  service={['devops-service.application.update']}
                >
                  <Tooltip
                    placement="bottom"
                    title={
                      <div>
                        {!record.synchro ? (
                          <FormattedMessage id="app.synch" />
                        ) : (
                          <Fragment>
                            {record.active ? (
                              <FormattedMessage id="edit" />
                            ) : (
                              <FormattedMessage id="app.start" />
                            )}
                          </Fragment>
                        )}
                      </div>
                    }
                  >
                    {record.active && record.synchro ? (
                      <Button
                        icon="mode_edit"
                        shape="circle"
                        size="small"
                        onClick={this.showSideBar.bind(this, 'edit', record.id)}
                      />
                    ) : (
                      <Icon
                        type="mode_edit"
                        className="c7n-app-icon-disabled"
                      />
                    )}
                  </Tooltip>
                </Permission>
                <Permission
                  type={type}
                  projectId={projectId}
                  organizationId={orgId}
                  service={['devops-service.application.queryByAppIdAndActive']}
                >
                  <Tooltip
                    placement="bottom"
                    title={
                      !record.synchro ? (
                        <FormattedMessage id="app.synch" />
                      ) : (
                        <Fragment>
                          {record.active ? (
                            <FormattedMessage id="app.stop" />
                          ) : (
                            <FormattedMessage id="app.run" />
                          )}
                        </Fragment>
                      )
                    }
                  >
                    {record.synchro ? (
                      <Button
                        shape="circle"
                        size="small"
                        onClick={this.changeAppStatus.bind(
                          this,
                          record.id,
                          record.active,
                        )}
                      >
                        {record.active ? (
                          <Icon type="remove_circle_outline" />
                        ) : (
                          <Icon type="finished" />
                        )}
                      </Button>
                    ) : (
                      <Fragment>
                        {record.active ? (
                          <Icon
                            type="remove_circle_outline"
                            className="c7n-app-icon-disabled"
                          />
                        ) : (
                          <Icon
                            type="finished"
                            className="c7n-app-icon-disabled"
                          />
                        )}
                      </Fragment>
                    )}
                  </Tooltip>
                </Permission>
              </Fragment>
            )}
          </Fragment>
        ),
      },
    ];
  };

  /**
   * 获取状态
   * @param text
   * @param record 表格中一个项目的记录
   * @returns {*}
   */
  getAppStatus = (text, record) => {
    const style = {
      fontSize: 18,
      marginRight: 6,
    };
    let icon = '';
    let msg = '';
    let color = '';
    if (record.fail) {
      icon = 'cancel';
      msg = 'failed';
      color = '#f44336';
    } else if (record.synchro && text) {
      icon = 'check_circle';
      msg = 'run';
      color = '#00bf96';
    } else if (text) {
      icon = 'timelapse';
      msg = 'creating';
      color = '#4d90fe';
    } else {
      icon = 'remove_circle';
      msg = 'stop';
      color = '#d3d3d3';
    }
    return (
      <span>
        <Icon style={{ color, ...style }} type={icon} />
        <FormattedMessage id={`app.${msg}`} />
      </span>
    );
  };

  /**
   * 切换应用id
   * @param id 应用id
   * @param status 状态
   */
  changeAppStatus = (id, status) => {
    const { AppStore } = this.props;
    const { projectId } = this.state;
    AppStore.changeAppStatus(projectId, id, !status).then(data => {
      if (data) {
        this.loadAllData(this.state.page);
      }
    });
  };

  /**
   * 校验应用的唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = (rule, value, callback) => {
    const { AppStore, intl } = this.props;
    const singleData = AppStore.singleData;
    const pa = /^\S+$/;
    if (value && pa.test(value)) {
      if ((singleData && value !== singleData.name) || !singleData) {
        this.postName(this.state.projectId, value, callback);
      } else {
        callback();
      }
    } else {
      callback(intl.formatMessage({ id: 'app.checkName' }));
    }
  };

  /**
   * 关闭操作框
   */
  hideSidebar = () => {
    const { AppStore, form } = this.props;
    AppStore.setSingleData(null);
    AppStore.setMbrInfo({
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    });
    this.setState({
      createSelectedRowKeys: [],
      createSelected: [],
      show: false,
      checked: true,
      harborId: undefined,
      chartId: undefined,
    });
    form.resetFields();
  };

  /**
   * 打开操作面板
   * @param type 操作类型
   * @param id 操作应用
   */
  showSideBar = (type, id = '') => {
    this.props.form.resetFields();
    const { AppStore } = this.props;
    const { projectId } = this.state;
    if (type === 'create') {
      AppStore.setSingleData(null);
      AppStore.loadSelectData(projectId);
      this.setState({ checked: true, show: true, type });
    } else {
      AppStore.loadDataById(projectId, id).then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setState({ checked: data.permission });
        }
      });
      AppStore.loadTagKeys(projectId, id);
      this.setState({ show: true, type, id });
    }
    AppStore.loadConfig(projectId);
    AppStore.loadPrm(projectId);
  };

  selectTemplate = (value, option) => {
    this.setState({ copyFrom: option.key });
  };

  /**
   * 处理页面跳转
   */
  linkToImport = () => {
    const { type, id: projectId, organizationId: orgId, name } = AppState.currentMenuType;
    const { history } = this.props;
    const url = `/devops/app/import?type=${type}&id=${projectId}&name=${name}&organizationId=${orgId}`;
    history.push(url);
  };

  render() {
    const {
      type,
      id: projectId,
      organizationId: orgId,
      name,
    } = AppState.currentMenuType;
    const {
      AppStore: {
        getAllData: serviceData,
        isRefresh,
        loading,
        getPageInfo,
        getInfo: { paras },
      },
      intl: { formatMessage },
    } = this.props;
    const {
      type: modeType,
      show,
      submitting,
      checked,
      createSelectedRowKeys,
      createSelected,
    } = this.state;

    // 当前页面的自动刷新是否开启
    this.initAutoRefresh('app');

    return (
      <Page
        className="c7n-region c7n-app-wrapper"
        service={[
          'devops-service.application.create',
          'devops-service.application.update',
          'devops-service.application.checkCode',
          'devops-service.application.checkName',
          'devops-service.application.pageByOptions',
          'devops-service.application.listTemplate',
          'devops-service.application.queryByAppIdAndActive',
          'devops-service.application.queryByAppId',
        ]}
      >
        {isRefresh ? (
          <LoadingBar display />
        ) : (
          <Fragment>
            <Header title={<FormattedMessage id="app.head" />}>
              <Permission
                service={['devops-service.application.create']}
                type={type}
                projectId={projectId}
                organizationId={orgId}
              >
                <Button
                  icon="get_app"
                  onClick={this.linkToImport}
                >
                  <FormattedMessage id="app.import" />
                </Button>
              </Permission>
              <Permission
                service={['devops-service.application.create']}
                type={type}
                projectId={projectId}
                organizationId={orgId}
              >
                <Button
                  icon="playlist_add"
                  onClick={this.showSideBar.bind(this, 'create')}
                >
                  <FormattedMessage id="app.create" />
                </Button>
              </Permission>
              <RefreshBtn name="app" onFresh={this.handleRefresh} />
            </Header>
            <Content code="app" values={{ name }}>
              <Table
                filterBarPlaceholder={formatMessage({ id: 'filter' })}
                pagination={getPageInfo}
                loading={loading}
                onChange={this.tableChange}
                columns={this.getColumn()}
                dataSource={serviceData}
                rowKey={record => record.id}
                filters={paras.slice()}
              />
            </Content>
          </Fragment>
        )}
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(AppHome)));
