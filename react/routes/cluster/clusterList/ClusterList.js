import React, { Component, Fragment } from 'react';
import { Link, withRouter } from "react-router-dom";
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Tooltip, Icon, Table, Pagination } from 'choerodon-ui';
import { Permission, stores } from '@choerodon/boot';
import _ from 'lodash';
import StatusTags from '../../../components/StatusTags';
import TimePopover from '../../../components/timePopover';
import MouserOverWrapper from '../../../components/MouseOverWrapper';
import './ClusterList.scss';

const { AppState } = stores;

@observer
class ClusterList extends Component {
  constructor(props) {
    super(...arguments);
    this.state = {
      page: 1,
      size: 10,
    };
  }

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    const { clusterId, tableData } = this.props;
    const { clusterId: nextClusterId, tableData: nextTableData } = nextProps;
    return !(nextClusterId === clusterId && _.isEqual(nextTableData, tableData));
  }

  /**
   * table 操作
   * @param pagination
   */
  tableChange = (pagination) => {
    const { clusterId, tableChange } = this.props;
    tableChange(pagination, clusterId);
  };

  /**
   * 页码改变的回调
   * @param page
   * @param size
   */
  onPageChange = (page, size) => {
    const { store, clusterId } = this.props;
    const { organizationId } = AppState.currentMenuType;
    this.setState({ page: page, size });
    store.loadMoreNode(organizationId, clusterId, page, size);
  };

  /**
   * 展开按钮
   * @param id
   */
  showMore = id => {
    const { store, showMore } = this.props;
    let activeKey = _.cloneDeep(store.getActiveKey);

    if (activeKey.includes(id)) {
      // remove active state
      _.pull(activeKey, id);

      const displayNodes = _.slice(store.getNodeData[id], 0, 3);
      store.setNodeData(id, displayNodes);
    } else {
      showMore(id);
      activeKey.push(id);
    }

    store.setActiveKey(activeKey);
  };

  renderCm = (record, type) => {
    const { intl: { formatMessage } } = this.props;

    return (<div className="c7n-cls-table-cm" >
      <span className="c7n-cls-up" />
      <Tooltip title={record[`${type}RequestPercentage`]} >
        <span className="c7n-cls-up-rv" >
          {formatMessage({ id: 'node.rv' })}：{record[`${type}Request`]}
        </span >
      </Tooltip >
      <span className="c7n-cls-down" />
      <Tooltip title={record[`${type}LimitPercentage`]} >
        {formatMessage({ id: 'node.lmv' })}：{record[`${type}Limit`]}
      </Tooltip >
    </div >);
  };

  render() {
    const {
      store,
      data: { id, upgrade, connect, nodes, name, description, code },
      tableData,
      showSideBar,
      clusterId,
      intl: { formatMessage },
      delClusterShow,
    } = this.props;
    const { type, organizationId, name: OrgName } = AppState.currentMenuType;
    const { getNodePageInfo, getActiveKey, getMoreLoading } = store;

    // numberOfElements 表示当前页数的数据条数
    // 当前页数 >= 3 总页数 > 3 ，显示展开与收起的按钮
    const { current, total, pageSize, numberOfElements } = getNodePageInfo[clusterId] || {
      current: nodes ? nodes.pageNum : 1,
      total: nodes ? nodes.total : 0,
      pageSize: nodes ? nodes.pageSize : 10,
      numberOfElements: nodes ? nodes.size : 0,
    };

    const columns = [{
      key: 'status',
      title: formatMessage({ id: 'status' }),
      dataIndex: 'status',
      width: 90,
      render: status => <StatusTags
        name={status}
        colorCode={status}
        style={{
          minWidth: 63,
        }}
      />,
    }, {
      key: 'nodeName',
      title: formatMessage({ id: 'cluster.node' }),
      render: record => (
        <Link
          to={{
            pathname: `/devops/cluster/${id}/node`,
            search: `?type=${type}&id=${organizationId}&name=${OrgName}&organizationId=${organizationId}&node=${record.nodeName}`,
          }}
        >
          <MouserOverWrapper text={record.nodeName} width={0.1} >{record.nodeName}</MouserOverWrapper >
        </Link >
      ),
    }, {
      title: formatMessage({ id: 'ist.expand.net.type' }),
      key: 'type',
      dataIndex: 'type',
      render: type => <MouserOverWrapper text={type} width={0.05} >{type}</MouserOverWrapper >,
    }, {
      key: 'cpuAllocatable',
      title: formatMessage({ id: 'cluster.cpu' }),
      render: record => this.renderCm(record, 'cpu'),
    }, {
      key: 'memoryAllocatable',
      title: formatMessage({ id: 'cluster.memory' }),
      render: record => this.renderCm(record, 'memory'),
    }, {
      key: 'createTime',
      title: formatMessage({ id: 'ciPipeline.createdAt' }),
      dataIndex: 'createTime',
      render: createTime => <TimePopover content={createTime} />,
    }];

    return (
      <Tooltip
        placement="bottom"
        title={upgrade ? <FormattedMessage id="cluster.status.update" /> : null}
      >
        <div className="c7n-cls-wrap" >
          <div className={`c7n-cls-head ${connect ? '' : 'c7n-cls-disconnect'}`} >
            <span className="c7n-cls-status-line" />
            <span className="c7n-cls-head-name" >{name}</span >
            <div className="c7n-cls-status-tag" >
              <div>{connect ? formatMessage({ id: 'running' }) : formatMessage({ id: 'disconnect' })}</div>
            </div >
            <span className="c7n-cls-head-des" >{description}</span >
            <div className="c7n-cls-head-action" >
              {connect ? null : <Permission
                service={['devops-service.devops-cluster.queryShell']}
                type={type}
                organizationId={organizationId}
              >
                <Tooltip title={<FormattedMessage id="cluster.active" />} >
                  <Button
                    funcType="flat"
                    shape="circle"
                    onClick={showSideBar.bind(this, 'key', id, name)}
                  >
                    <Icon type="vpn_key" />
                  </Button >
                </Tooltip >
              </Permission >}
              <Permission
                service={['devops-service.devops-cluster.update']}
                type={type}
                organizationId={organizationId}
              >
                <Tooltip title={<FormattedMessage id="cluster.edit" />} >
                  <Button
                    funcType="flat"
                    shape="circle"
                    onClick={showSideBar.bind(this, 'edit', id, name)}
                  >
                    <Icon type="mode_edit" />
                  </Button >
                </Tooltip >
              </Permission >
              {connect ? null : <Permission
                service={['devops-service.devops-cluster.deleteCluster']}
                type={type}
                organizationId={organizationId}
              >
                <Tooltip title={<FormattedMessage id="cluster.del" />} >
                  <Button
                    funcType="flat"
                    shape="circle"
                    onClick={() => delClusterShow(id, name, code)}
                  >
                    <Icon type="delete_forever" />
                  </Button >
                </Tooltip >
              </Permission >}
            </div >
          </div >
          {connect ? <Fragment >
            {nodes && nodes.list.length ?
              <Table
                className="c7n-cls-node-table"
                loading={!!getMoreLoading[clusterId]}
                filterBar={false}
                bordered={false}
                columns={columns}
                dataSource={tableData}
                pagination={false}
                rowKey={record => record.nodeName}
              /> : null}
            <div className="c7n-cls-node-table-footer" >
              {numberOfElements >= 3 && total > 3 ?
                <Button
                  disabled={!!getMoreLoading[clusterId]}
                  className="c7n-cls-node-more"
                  onClick={this.showMore.bind(this, id)}
                >
                  {getActiveKey.includes(id) ? formatMessage({ id: "shrink" }) : formatMessage({ id: "expand" })}
                </Button > : null}
              {getActiveKey.includes(id) && total > 10 ? <Pagination
                showSizeChanger
                total={total}
                current={current}
                pageSize={pageSize}
                className="c7n-cls-node-pg"
                onChange={this.onPageChange}
                onShowSizeChange={this.onPageChange}
              /> : null}
            </div >
          </Fragment > : null}
        </div >
      </Tooltip >
    );
  }
}

export default withRouter(injectIntl(ClusterList));
