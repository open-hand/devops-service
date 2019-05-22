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
import './AppHome.scss';
import '../../../main.scss';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import '../../envPipeline/EnvPipeLineHome.scss';
import Tips from '../../../../components/Tips/Tips';
import InterceptMask from '../../../../components/interceptMask/InterceptMask';
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
  postName = _.debounce((projectId, value, callback) => {
    const { AppStore, intl } = this.props;
    AppStore.checkName(projectId, value).then(data => {
      if (data && data.failed) {
        callback(intl.formatMessage({ id: 'template.checkName' }));
      } else {
        callback();
      }
    });
  }, 600);

  /**
   * 校验应用编码规则
   * @param rule
   * @param value
   * @param callback
   */
  checkCode = _.debounce((rule, value, callback) => {
    const {
      AppStore,
      intl: { formatMessage },
    } = this.props;
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      AppStore.checkCode(this.state.projectId, value).then(data => {
        if (data && data.failed) {
          callback(formatMessage({ id: 'template.checkCode' }));
        } else {
          callback();
        }
      });
    } else {
      callback(formatMessage({ id: 'template.checkCodeReg' }));
    }
  }, 600);

  constructor(props) {
    const menu = AppState.currentMenuType;
    const {
      location: { state },
    } = props.history;
    super(props);
    this.state = {
      page: 0,
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
    show && modeType && this.showSideBar("create");
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
   * 提交数据
   * @param e
   */
  handleSubmit = e => {
    e.preventDefault();
    const { AppStore, form } = this.props;
    const { projectId, id, type, page, checked, createSelectedRowKeys } = this.state;
    const tagKeys = AppStore.getTagKeys;

    if (type === 'create') {
      form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const postData = data;
          postData.projectId = projectId;
          postData.isSkipCheckPermission = checked;
          postData.userIds = createSelectedRowKeys;

          this.setState({ submitting: true });
          AppStore.addData(projectId, postData)
            .then(res => {
              if (res && res.failed) {
                Choerodon.prompt(res.message);
                this.setState({ submitting: false });
              } else {
                this.loadAllData(page);
                AppStore.setMbrInfo({ filters: {}, sort: { columnKey: 'id', order: 'descend' }, paras: [] });

                this.setState({
                  type: false,
                  show: false,
                  submitting: false,
                  createSelectedRowKeys: [],
                  createSelected: [],
                  harborId: undefined,
                  chartId: undefined,
                });
              }
            })
            .catch(err => {
              this.setState({ submitting: false });
              Choerodon.handleResponseError(err);
            });
        }
      });
    } else if (type === 'edit') {
      form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const formData = data;
          const userIds = _.map(tagKeys, t => t.iamUserId);
          formData.isSkipCheckPermission = checked;
          formData.id = id;
          formData.userIds = userIds;

          this.setState({ submitting: true });
          AppStore.updateData(projectId, formData)
            .then(res => {
              if (res && res.failed) {
                Choerodon.prompt(res.message);
                this.setState({ submitting: false });
              } else {
                this.handleRefresh();

                AppStore.setMbrInfo({
                  filters: {},
                  sort: { columnKey: 'id', order: 'descend' },
                  paras: [],
                });

                this.setState(
                  {
                    show: false,
                    submitting: false,
                    harborId: undefined,
                    chartId: undefined,
                  }, () => {
                    AppStore.setTagKeys([]);
                  },
                );
              }
            })
            .catch(err => {
              this.setState({ submitting: false });
              Choerodon.handleResponseError(err);
            });
        }
      });
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

  cbChange = e => {
    this.setState({ checked: e.target.value });
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  mbrTableChange = (pagination, filters, sorter, paras) => {
    const { AppStore } = this.props;
    const { id } = AppState.currentMenuType;
    AppStore.setMbrInfo({
      filters,
      sort: sorter,
      paras,
    });
    let sort = {
      field: '',
      order: 'desc',
    };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let searchParam = {};
    let page = pagination.current - 1;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    AppStore.loadPrm(id, page, pagination.pageSize, sort, postData);
  };

  onCreateSelectChange = (keys, selected) => {
    let s = [];
    const a = this.state.createSelectedTemp.concat(selected);
    this.setState({ createSelectedTemp: a });
    _.map(keys, o => {
      if (_.filter(a, ['iamUserId', o]).length) {
        s.push(_.filter(a, ['iamUserId', o])[0]);
      }
    });
    this.setState({
      createSelectedRowKeys: keys,
      createSelected: s,
    });
  };

  /**
   * 分配权限
   * @param keys
   * @param selected
   */
  onSelectChange = (keys, selected) => {
    const { AppStore } = this.props;
    const { getTagKeys: tagKeys } = AppStore;
    let s = [];
    const a = tagKeys.length
      ? tagKeys.concat(selected)
      : this.state.selected.concat(selected);
    this.setState({ selected: a });
    _.map(keys, o => {
      if (_.filter(a, ['iamUserId', o]).length) {
        s.push(_.filter(a, ['iamUserId', o])[0]);
      }
    });
    AppStore.setTagKeys(s);
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
        singleData,
        selectData,
        getAllData: serviceData,
        isRefresh,
        loading,
        getPageInfo,
        getMbrPageInfo,
        getTableLoading: tableLoading,
        getTagKeys: tagKeys,
        getMbr,
        getInfo: { paras },
        getMbrInfo: { filters, paras: mbrParas },
        getHarborList,
        getChartList,
      },
      intl: { formatMessage },
      form: { getFieldDecorator },
    } = this.props;
    const {
      type: modeType,
      show,
      submitting,
      id,
      checked,
      createSelectedRowKeys,
      createSelected,
    } = this.state;

    // 当前页面的自动刷新是否开启
    this.initAutoRefresh('app');

    const rowCreateSelection = {
      selectedRowKeys: createSelectedRowKeys,
      onChange: this.onCreateSelectChange,
    };

    const rowSelection = {
      selectedRowKeys: _.map(tagKeys, s => s.iamUserId),
      onChange: this.onSelectChange,
    };

    const tagCreateDom = _.map(createSelected, t => (
      <Tag className="c7n-env-tag" key={t.iamUserId}>
        {t.loginName} {t.realName}
      </Tag>
    ));

    const tagDom = _.map(tagKeys, t => {
      if (t) {
        return (
          <Tag className="c7n-env-tag" key={t.iamUserId}>
            {t.loginName} {t.realName}
          </Tag>
        );
      }
      return null;
    });

    const columns = [
      {
        key: 'loginName',
        filters: [],
        filteredValue: filters.loginName || [],
        title: formatMessage({
          id: 'envPl.loginName',
        }),
        dataIndex: 'loginName',
      },
      {
        key: 'realName',
        filters: [],
        filteredValue: filters.realName || [],
        title: formatMessage({
          id: 'envPl.userName',
        }),
        dataIndex: 'realName',
      },
    ];

    let initHarbor = undefined;
    if (getHarborList.length) {
      const hasProject = _.find(getHarborList, item => item.name.indexOf('project') !== -1);
      initHarbor = hasProject ? hasProject.id : getHarborList[0].id
    }
    let initChart = getChartList.length ? getChartList[0].id : undefined;
    if (singleData && singleData.gitlabProjectId) {
      const { harborConfigId, chartConfigId } = singleData;
      const hasHarbor = _.find(getHarborList, ['id', harborConfigId]);
      const hasChart = _.find(getChartList, ['id', chartConfigId]);
      initHarbor = hasHarbor ? harborConfigId : undefined;
      initChart = hasChart ? chartConfigId : undefined;
    }

    const formContent = (
      <React.Fragment>
        <Form layout="vertical" className="c7n-sidebar-form">
          <div className="c7ncd-sidebar-select">
            <FormItem {...formItemLayout}>
              {getFieldDecorator('type', {
                initialValue: singleData ? singleData.type : 'normal',
              })(
                <Select
                  key="service"
                  label={<FormattedMessage id="app.chooseType" />}
                  dropdownMatchSelectWidth
                  disabled={modeType !== 'create'}
                  size="default"
                >
                  {['normal', 'test'].map(s => (
                    <Option value={s} key={s}>
                      <FormattedMessage id={`app.type.${s}`} />
                    </Option>
                  ))}
                </Select>,
              )}
            </FormItem>
            <Tips type="form" data="app.chooseType.tip" />
          </div>
          {modeType === 'create' && (
            <FormItem {...formItemLayout}>
              {getFieldDecorator('code', {
                rules: [
                  {
                    required: modeType === 'create',
                    whitespace: true,
                    max: 47,
                    message: formatMessage({ id: 'required' }),
                  },
                  {
                    validator: modeType === 'create' ? this.checkCode : null,
                  },
                ],
              })(
                <Input
                  maxLength={30}
                  label={<FormattedMessage id="app.code" />}
                  size="default"
                  suffix={<Tips type="form" data="app.code.tooltip" />}
                />,
              )}
            </FormItem>
          )}
          <FormItem {...formItemLayout}>
            {getFieldDecorator('name', {
              rules: [
                {
                  required: true,
                  whitespace: true,
                  message: formatMessage({ id: 'required' }),
                },
                {
                  validator: this.checkName,
                },
              ],
              initialValue: singleData ? singleData.name : '',
            })(
              <Input
                maxLength={20}
                label={<FormattedMessage id="app.name" />}
                size="default"
              />,
            )}
          </FormItem>
          {modeType === 'create' && (
            <div className="c7ncd-sidebar-select">
              <FormItem {...formItemLayout}>
                {getFieldDecorator('applicationTemplateId', {
                  rules: [
                    {
                      message: formatMessage({ id: 'required' }),
                      transform: value => {
                        if (value) {
                          return value.toString();
                        }
                        return value;
                      },
                    },
                  ],
                })(
                  <Select
                    filter
                    allowClear
                    label={<FormattedMessage id="app.chooseTem" />}
                    dropdownClassName="c7n-app-select-dropdown"
                    onSelect={this.selectTemplate}
                    filterOption={(input, option) =>
                      option.props.children.props.children
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
                    }
                  >
                    {selectData.map(s => (
                      <Option value={s.id} key={s.id}>
                        <Tooltip placement="right" title={s.description}>
                          {s.name}
                        </Tooltip>
                      </Option>
                    ))}
                  </Select>,
                )}
              </FormItem>
              <Tips type="form" data="app.chooseTem.tip" />
            </div>
          )}
        </Form>
        {(singleData && singleData.gitlabProjectId || modeType === 'create') ? (<Fragment>
          <div className="c7n-env-tag-title">
            <FormattedMessage id="app.config" />
            <Popover
              overlayStyle={{ maxWidth: '350px' }}
              content={formatMessage({ id: 'app.config.help' })}
            >
              <Icon type="help" />
            </Popover>
          </div>
          <div className="c7n-app-config-panel">
            <FormItem
              className="c7n-select_480"
              {...formItemLayout}
            >
              {getFieldDecorator('harborConfigId', {
                initialValue: initHarbor,
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
              })(
                <Select
                  filter
                  showSearch
                  className="c7n-select_480"
                  optionFilterProp="children"
                  label={<FormattedMessage id="app.form.selectDocker" />}
                  getPopupContainer={triggerNode => triggerNode.parentNode}
                  filterOption={(input, option) =>
                    option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {_.map(getHarborList, item => (<Option value={item.id} key={item.id}>
                    {item.name}
                  </Option>))}
                </Select>,
              )}
            </FormItem>
            <FormItem
              className="c7n-select_480"
              {...formItemLayout}
            >
              {getFieldDecorator('chartConfigId', {
                initialValue: initChart,
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  },
                ],
              })(
                <Select
                  filter
                  className="c7n-select_480"
                  optionFilterProp="children"
                  label={<FormattedMessage id="app.form.selectHelm" />}
                  getPopupContainer={triggerNode => triggerNode.parentNode}
                  filterOption={(input, option) =>
                    option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {_.map(getChartList, item => (<Option value={item.id} key={item.id}>
                    {item.name}
                  </Option>))}
                </Select>,
              )}
            </FormItem>
          </div>
          <div className="c7n-app-config-warn">
            <Icon type="error" className="c7n-app-config-warn-icon" />
            <FormattedMessage id="app.config.warn" />
          </div>
          <div className="c7n-env-tag-title">
            <FormattedMessage id="app.authority" />
            <Popover
              overlayStyle={{ maxWidth: '350px' }}
              content={formatMessage({ id: 'app.authority.help' })}
            >
              <Icon type="help" />
            </Popover>
          </div>
          <div className="c7n-app-authority-radio">
            <RadioGroup
              label={<FormattedMessage id="app.authority.label" />}
              onChange={this.cbChange}
              value={checked}
            >
              <Radio value={true}>
                <FormattedMessage id="app.mbr.all" />
              </Radio>
              <Radio value={false}>
                <FormattedMessage id="app.mbr.part" />
              </Radio>
            </RadioGroup>
          </div>
          {checked ? null : (
            <div>
              <div className="c7n-sidebar-form">
                <Table
                  className="c7n-env-noTotal"
                  rowSelection={
                    modeType === 'create' ? rowCreateSelection : rowSelection
                  }
                  columns={columns}
                  dataSource={getMbr}
                  filterBarPlaceholder={formatMessage({ id: 'filter' })}
                  pagination={getMbrPageInfo}
                  loading={tableLoading}
                  onChange={this.mbrTableChange}
                  rowKey={record => record.iamUserId}
                  filters={mbrParas.slice()}
                />
              </div>
              <div className="c7n-env-tag-title">
                <FormattedMessage id="app.authority.mbr" />
              </div>
              <div className="c7n-env-tag-wrap">
                {modeType === 'create' ? tagCreateDom : tagDom}
              </div>
            </div>
          )}
        </Fragment>) : null}
      </React.Fragment>
    );

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
              {show && (
                <Sidebar
                  title={
                    <FormattedMessage
                      id={modeType === 'create' ? 'app.create' : 'app.edit'}
                    />
                  }
                  visible={show}
                  onOk={this.handleSubmit}
                  okText={
                    <FormattedMessage
                      id={modeType === 'create' ? 'create' : 'save'}
                    />
                  }
                  cancelText={<FormattedMessage id="cancel" />}
                  confirmLoading={submitting}
                  onCancel={this.hideSidebar}
                >
                  <Content
                    code={`app.${modeType}`}
                    values={{ name: singleData ? singleData.name : name }}
                    className="sidebar-content"
                  >
                    {formContent}
                    <InterceptMask visible={submitting} />
                  </Content>
                </Sidebar>
              )}
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
