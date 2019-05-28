import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Table,
  Button,
  Modal,
  Tooltip,
  Select,
} from 'choerodon-ui';
import {
  Content,
  Header,
  Page,
  Permission,
} from '@choerodon/boot';
import _ from 'lodash';
import TimePopover from '../../../../components/timePopover';
import '../../../main.scss';
import './PipelineRecord.scss';
import StatusTags from '../../../../components/StatusTags/StatusTags';
import { HEIGHT } from '../../../../common/Constants';
import { RELATED_TO_ME, STATUS_COLOR } from "../components/Constants";

const { Option } = Select;

@injectIntl
@withRouter
@inject('AppState')
@observer
class PipelineRecord extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      pipelineId: null,
      id: null,
      name: null,
      checkData: {},
      show: false,
      showRetry: false,
      passLoading: false,
      stopLoading: false,
      submitting: false,
      canCheck: false,
      checkTips: null,
      canRetry: true,
      searchData: null,
    };
  }

  componentDidMount() {
    const {
      PipelineRecordStore,
      AppState: { currentMenuType: { projectId } },
      location: { state },
    } = this.props;
    PipelineRecordStore.loadPipelineData(projectId);
    const { pipelineId } = state || {};
    PipelineRecordStore.loadRecordList(projectId, pipelineId, 0, HEIGHT < 900 ? 10 : 15);
    pipelineId && this.setState({ pipelineId });
  }

  /**
   * 加载流水线执行总览列表
   */
  loadData = () => {
    const {
      PipelineRecordStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const { pipelineId, searchData } = this.state;
    PipelineRecordStore.setInfo({
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    });
    PipelineRecordStore.loadRecordList(projectId, pipelineId, 0, HEIGHT < 900 ? 10 : 15, searchData);
  };

  /**
   * 处理刷新函数
   */
  handleRefresh = () => {
    const { PipelineRecordStore } = this.props;
    const pageInfo = PipelineRecordStore.getPageInfo;
    const { filters, sort, paras } = PipelineRecordStore.getInfo;
    this.tableChange(pageInfo, filters, sort, paras);
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const {
      PipelineRecordStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const { pipelineId, searchData } = this.state;
    PipelineRecordStore.setInfo({ filters, sort: sorter, paras });
    const sort = { field: 'id', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let searchParam = {};
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    PipelineRecordStore.loadRecordList(
      projectId,
      pipelineId,
      pagination.current - 1,
      pagination.pageSize,
      searchData,
      sort,
      postData,
    );
  };

  /**
   * 选择流水线
   */
  handleSelect = (value) => {
    const { PipelineRecordStore } = this.props;
    PipelineRecordStore.setInfo({
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    });
    this.setState({ pipelineId: value }, () => this.loadData());
  };

  /**
   * 获取表格行
   */
  getColumns = () => {
    const {
      PipelineRecordStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          projectId,
          type,
          organizationId,
        },
      },
    } = this.props;
    const {
      filters,
      sort: { columnKey, order },
    } = PipelineRecordStore.getInfo;
    return [
      {
        title: formatMessage({ id: 'pipelineRecord.pipeline.status' }),
        key: 'status',
        dataIndex: 'status',
        sorter: true,
        sortOrder: columnKey === 'status' && order,
        filters: _.map(['success', 'failed', 'running', 'stop', 'pendingcheck', 'deleted'], item => (
          {
            text: formatMessage({ id: `pipelineRecord.status.${item}` }),
            value: item,
          }
        )),
        filteredValue: filters.status || [],
        render: text => (
          <StatusTags name={formatMessage({ id: `pipelineRecord.status.${text}` })} color={STATUS_COLOR[text]} />),
      },
      {
        title: formatMessage({ id: 'pipeline.trigger' }),
        key: 'triggerType',
        dataIndex: 'triggerType',
        sorter: true,
        sortOrder: columnKey === 'triggerType' && order,
        filters: [
          {
            text: formatMessage({ id: 'pipeline.trigger.auto' }),
            value: 'auto',
          },
          {
            text: formatMessage({ id: 'pipeline.trigger.manual' }),
            value: 'manual',
          },
        ],
        filteredValue: filters.triggerType || [],
        render: text => (<FormattedMessage id={`pipeline.trigger.${text}`} />),
      },
      {
        title: formatMessage({ id: 'pipelineRecord.pipeline.name' }),
        key: 'pipelineName',
        dataIndex: 'pipelineName',
      },
      {
        title: formatMessage({ id: 'pipelineRecord.process' }),
        key: 'stageDTOList',
        dataIndex: 'stageDTOList',
        render: this.getProcess,
      },
      {
        title: <FormattedMessage id="ist.expand.date" />,
        dataIndex: 'lastUpdateDate',
        key: 'lastUpdateDate',
        render: text => <TimePopover content={text} />,
      },
      {
        key: 'action',
        align: 'right',
        render: (text, record) => {
          const { status, type: checkType, id, pipelineName, stageName, stageRecordId, taskRecordId, pipelineId, index } = record;
          return (<div>
              {index && status === 'failed' && (
                <Permission
                  type={type}
                  projectId={projectId}
                  organizationId={organizationId}
                  service={['devops-service.pipeline.retry']}
                >
                  <Tooltip
                    placement="bottom"
                    title={<FormattedMessage id="pipelineRecord.retry" />}
                  >
                    <Button
                      icon="replay"
                      shape="circle"
                      size="small"
                      onClick={this.openRetry.bind(this, id, pipelineName)}
                    />
                  </Tooltip>
                </Permission>
              )}
              {index && status === 'pendingcheck' && (
                <Permission
                  type={type}
                  projectId={projectId}
                  organizationId={organizationId}
                  service={['devops-service.pipeline.audit']}
                >
                  <Tooltip
                    placement="bottom"
                    title={<FormattedMessage id="pipelineRecord.check.manual" />}
                  >
                    <Button
                      icon="authorize"
                      shape="circle"
                      size="small"
                      onClick={this.showSidebar.bind(this, id, checkType, pipelineName, stageName, stageRecordId, taskRecordId)}
                    />
                  </Tooltip>
                </Permission>
              )}
              <Permission
                type={type}
                projectId={projectId}
                organizationId={organizationId}
                service={['devops-service.pipeline.getRecordById']}
              >
                <Tooltip
                  placement="bottom"
                  title={<FormattedMessage id="pipelineRecord.detail" />}
                >
                  <Button
                    icon="find_in_page"
                    shape="circle"
                    size="small"
                    onClick={this.linkToDetail.bind(this, id, pipelineId)}
                  />
                </Tooltip>
              </Permission>
            </div>
          );
        },
      },
    ];
  };

  /**
   * 获取流程列
   * @param stageDTOList
   * @param record
   * @returns {*}
   */
  getProcess = (stageDTOList, record) => {
    const { type, stageRecordId, status: pipelineStatus } = record;
    return (
      <div className="c7n-pipelineRecord-process">
        {
          _.map(stageDTOList, ({ status, id }) => {
            return (<div key={id} className="c7n-process-content">
              <span className={`c7n-process-line ${stageRecordId === id && type === "stage" ? `c7n-process-line-${pipelineStatus}` : ""}`} />
              <span className={`c7n-process-status c7n-process-status-${status}`} />
            </div>);
          })
        }
      </div>
    );
  };

  /**
   * 跳转到流水线详情
   * @param recordId
   * @param pId
   * @param name
   */
  linkToDetail = (recordId, pId) => {
    const {
      history,
      location: {
        search,
        state,
      },
    } = this.props;
    const { fromPipeline } = state || {};

    // 流水线进行过筛选
    const { pipelineId } = this.state;

    history.push({
      pathname: `/devops/pipeline-record/detail/${pId}/${recordId}`,
      search,
      state: {
        pipelineId: pId,
        isFilter: !!pipelineId,
        fromPipeline,
      },
    });
  };

  /**
   * 处理重新执行操作
   */
  handleRetry = () => {
    const {
      PipelineRecordStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const { id } = this.state;
    this.setState({ submitting: true });
    PipelineRecordStore.retry(projectId, id)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else if (data) {
          this.setState({ showRetry: false, id: null, canRetry: true });
          this.loadData();
        } else {
          this.setState({ canRetry: false });
        }
        this.setState({ submitting: false });
      })
      .catch(e => {
        Choerodon.handleResponseError(e);
        this.setState({ submitting: false });
      })
  };

  /**
   * 中止或通过人工审核
   * @param flag 是否通过
   */
  handleSubmit = (flag) => {
    const {
      PipelineRecordStore,
      AppState: {
        currentMenuType: { projectId },
        userInfo: { id: userId },
      },
      intl: { formatMessage },
    } = this.props;
    const {
      id,
      checkData: {
        stageRecordId,
        taskRecordId,
        checkType,
      },
    } = this.state;
    const data = {
      pipelineRecordId: id,
      userId,
      isApprove: flag,
      type: checkType,
      stageRecordId,
      taskRecordId,
    };
    this.setState({ [flag ? 'passLoading' : 'stopLoading']: true });
    PipelineRecordStore.checkData(projectId, data)
      .then(data => {
        if (data) {
          if (data.failed) {
            Choerodon.prompt(data.message);
          } else if (data.length){
            //会签，非最后一人审核，返回数据：[{ audit: true 已审核 | false 未审核, loginName: "工号", realName: "姓名"}]
            const users = {
              check: [],
              unCheck: [],
            };
            _.forEach(data, ({ audit, loginName, realName }) => {
              users[audit ? "check" : "unCheck"].push(`${loginName} ${realName}`);
            });
            this.setState({
              canCheck: false,
              checkTips: formatMessage({ id: "pipeline.check.tips.text"}, {checkUsers: users["check"].join("，"), unCheckUsers: users["unCheck"].join("，")}),
            })
          } else {
            // 或签、会签最后一人，返回数据[]
            this.handClose(true);
          }
        }
        this.setState({ [flag ? 'passLoading' : 'stopLoading']: false });
      });
  };

  /**
   * 展开重试弹窗
   * @param id 流水线执行记录id
   */
  openRetry = (id, name) => {
    this.setState({ showRetry: true, id, name });
  };

  /**
   * 关闭重试弹窗
   */
  closeRetry = () => {
    this.setState({ showRetry: false, id: null, name: null, canRetry: true });
  };

  /**
   * 展开弹窗
   * @param checkType 阶段间或人工卡点时审核
   * @param id 流水线执行记录id
   * @param name 流水线名称
   * @param stageName 流阶段名称
   * @param stageRecordId 阶段id
   * @param taskRecordId 任务id
   */
  showSidebar = (id = null, checkType, name, stageName, stageRecordId = null, taskRecordId = null) => {
    const {
      PipelineRecordStore,
      AppState: {
        currentMenuType: { projectId },
        userInfo: { id: userId },
      },
      intl: { formatMessage },
    } = this.props;
    const data = {
      pipelineRecordId: id,
      userId,
      type: checkType,
      stageRecordId,
      taskRecordId,
    };
    PipelineRecordStore.canCheck(projectId, data)
      .then(data =>{
        if (data) {
          if (data.failed) {
            Choerodon.prompt(data.message);
          } else if ((data.isCountersigned || data.isCountersigned === 0) && data.userName) {
            // 会签已被终止、或签已被审核，返回数据：{ isCountersigned: 0 或签 | 1 会签, userName: "string"}
            this.setState({
              show: true,
              canCheck: false,
              checkTips: formatMessage({ id: `pipeline.canCheck.tips.${data.isCountersigned}`}, {userName: data.userName}),
            })
          } else {
            // 预检通过，返回数据：{ isCountersigned: null, userName: null }
            this.setState({
              show: true,
              id,
              name,
              checkData: {
                checkType,
                stageName,
                stageRecordId,
                taskRecordId,
              },
              canCheck: true,
            });
          }
        }
      });
  };

  /**
   * 关闭弹窗
   * @param flag 是否重新加载列表数据
   */
  handClose = (flag) => {
    if (flag) {
      this.loadData();
    }
    this.setState({ show: false, id: null, name: null, checkData: {}, canCheck: false, checkTips: null });
  };

  /**
   * 与我相关搜索
   * @param value
   */
  handleSearch = (value) => {
    this.setState({ searchData: value }, () => this.loadData());
  };

  render() {
    const {
      PipelineRecordStore,
      intl: { formatMessage },
      location: {
        state,
        search,
      },
      AppState: {
        currentMenuType: {
          name,
        },
      },
    } = this.props;
    const {
      pipelineId,
      show,
      passLoading,
      stopLoading,
      name: pipelineName,
      checkData: {
        stageName,
        checkType,
      },
      showRetry,
      submitting,
      canCheck,
      checkTips,
      canRetry,
    } = this.state;

    const { loading, pageInfo } = PipelineRecordStore;
    const pipelineData = PipelineRecordStore.getPipelineData;
    const data = PipelineRecordStore.getRecordList;
    const { paras } = PipelineRecordStore.getInfo;

    const { fromPipeline } = state || {};
    const backPath = fromPipeline ? `/devops/pipeline${search}` : null;

    return (
      <Page
        className="c7n-region c7n-pipelineRecord-wrapper"
        service={[
          'devops-service.pipeline.listRecords',
          'devops-service.pipeline.listPipelineDTO',
          'devops-service.pipeline.getRecordById',
          'devops-service.pipeline.retry',
          'devops-service.pipeline.audit',
        ]}
      >
        <Header
          title={formatMessage({ id: 'pipelineRecord.header' })}
          backPath={backPath}
        >
          <Button
            onClick={this.handleRefresh}
            icon="refresh"
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content code="pipelineRecord" values={{ name }}>
          <div className="c7n-pipelineRecord-select">
            <Select
              label={formatMessage({ id: 'pipelineRecord.pipeline.name' })}
              className="c7n-pipelineRecord-select-200 mg-right-16"
              optionFilterProp="children"
              onChange={this.handleSelect}
              value={pipelineId || undefined}
              filter
              allowClear
              filterOption={(input, option) =>
                option.props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0
              }
            >
              {
                _.map(pipelineData, ({ id, name }) => (
                  <Option
                    key={id}
                    value={id}
                  >
                    {name}
                  </Option>
                ))
              }
            </Select>
            <Select
              mode="multiple"
              label={formatMessage({ id: "pipelineRecord.search" })}
              allowClear
              className="c7n-pipelineRecord-select-380"
              onChange={this.handleSearch}
            >
              {
                _.map(RELATED_TO_ME, item => (
                  <Option
                    key={item}
                    value={item}
                  >
                    {formatMessage({ id: `pipelineRecord.search.${item}` })}
                  </Option>
                ))
              }
            </Select>
          </div>
          <Table
            filterBarPlaceholder={formatMessage({ id: 'filter' })}
            loading={loading}
            onChange={this.tableChange}
            pagination={pageInfo}
            columns={this.getColumns()}
            dataSource={data}
            rowKey={record => record.id}
            filters={paras.slice()}
          />
        </Content>
        {showRetry && (
          <Modal
            confirmLoading={submitting}
            visible={showRetry}
            title={`${formatMessage({ id: 'pipelineRecord.retry.title' }, { name: pipelineName })}`}
            closable={false}
            onOk={this.handleRetry}
            onCancel={this.closeRetry}
            footer={canRetry ? [
              <Button
                key="back"
                onClick={this.closeRetry}
                disabled={submitting}
              >
                <FormattedMessage id="cancel" />
              </Button>,
              <Button
                key="submit"
                loading={submitting}
                type="primary"
                onClick={this.handleRetry}
              >
                <FormattedMessage id="ok" />
              </Button>,
            ] : [
              <Button
                key="back"
                type="primary"
                onClick={this.closeRetry}
              >
                <FormattedMessage id="pipelineRecord.check.tips.button" />
              </Button>,
            ]}
          >
            <div className="c7n-padding-top_8">
              <FormattedMessage id={`pipelineRecord.retry.${canRetry}`} />
            </div>
          </Modal>
        )}
        {show && (
          <Modal
            visible={show}
            title={formatMessage({ id: 'pipelineRecord.check.manual' })}
            closable={false}
            footer={canCheck ? [
              <Button
                key="back"
                onClick={this.handClose.bind(this, false)}
                disabled={stopLoading || passLoading}
              >
                <FormattedMessage id="pipelineRecord.check.cancel" />
              </Button>,
              <Button
                key="stop"
                loading={stopLoading}
                type="primary"
                onClick={this.handleSubmit.bind(this, false)}
                disabled={passLoading}
              >
                <FormattedMessage id="pipelineRecord.check.stop" />
              </Button>,
              <Button
                key="pass"
                loading={passLoading}
                type="primary"
                onClick={this.handleSubmit.bind(this, true)}
                disabled={stopLoading}
              >
                <FormattedMessage id="pipelineRecord.check.pass" />
              </Button>,
            ] : [
              <Button
                key="back"
                type="primary"
                onClick={this.handClose.bind(this, true)}
              >
                <FormattedMessage id="pipelineRecord.check.tips.button" />
              </Button>,
            ]}
          >
            <div className="c7n-padding-top_8">
              {canCheck ?
                <FormattedMessage
                  id={`pipelineRecord.check.${checkType}.des`}
                  values={{ name: pipelineName, stage: stageName }}
                /> :
                <span>{checkTips}</span>
              }
            </div>
          </Modal>
        )}
      </Page>
    );
  }
}

export default PipelineRecord;
