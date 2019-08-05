import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Modal } from 'choerodon-ui';
import { stores, Content } from '@choerodon/boot';
import _ from 'lodash';
import TimePopover from '../../../../components/timePopover';
import '../../../main.scss';
import './index.scss';

const Sidebar = Modal.Sidebar;
const { AppState } = stores;

@observer
class VersionTable extends Component {
  state = {
    selectedRowKeys: [],
    allSelectedRows: [],
  };

  componentDidMount() {
    const {
      store: { getAppDetailById, loadAllVersion },
    } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    this.handleSelectData();
    loadAllVersion({ projectId, appId: getAppDetailById.id });
  }

  handleRowSelect = (selectedRowKeys, selectedRows) => {
    const { allSelectedRows } = this.state;
    const _allSelectedRows = allSelectedRows.concat(selectedRows);

    this.setState({
      allSelectedRows: _allSelectedRows,
      selectedRowKeys,
    });
  };

  /**
   * 获取未发布版本
   * @returns {*}
   */
  getSidebarTable = () => {
    const {
      store: { getVersionData, loading, versionPage },
      intl: { formatMessage },
    } = this.props;
    const columns = [
      {
        title: <FormattedMessage id="deploy.ver" />,
        dataIndex: 'version',
      },
      {
        title: <FormattedMessage id="app.createTime" />,
        render: (text, record) => <TimePopover content={record.creationDate} />,
      },
    ];
    const rowSelection = {
      selectedRowKeys: this.state.selectedRowKeys || [],
      onChange: this.handleRowSelect,
    };
    return (
      <Table
        filterBarPlaceholder={formatMessage({ id: 'filter' })}
        className="c7n-table-512"
        loading={loading}
        pagination={versionPage}
        rowSelection={rowSelection}
        columns={columns}
        dataSource={getVersionData}
        rowKey={record => record.id}
        onChange={this.versionTableChange}
      />
    );
  };

  /**
   * table app表格搜索
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   */
  versionTableChange = (pagination, filters, sorter, paras) => {
    const {
      store: { getAppDetailById, loadAllVersion },
    } = this.props;
    const { id: projectId } = AppState.currentMenuType;

    let searchParam = {};
    const page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }

    const postData = {
      searchParam,
      param: paras.toString(),
    };

    loadAllVersion({
      projectId,
      postData,
      appId: getAppDetailById.id,
      page,
      size: pagination.pageSize,
    });
  };

  handleSelectData = () => {
    const selectData = _.map(this.props.idnex.selectData, 'id') || [];
    this.setState({ selectedRowKeys: selectData });
  };

  /**
   * 关闭弹框
   */
  handleClose = () => {
    this.props.idnex.changeShow(false);
  };

  /**
   * 添加版本
   */
  handleAddVersion = () => {
    const { store } = this.props;
    const { allSelectedRows, selectedRowKeys } = this.state;
    const selectedRows = _.map(selectedRowKeys, item => _.find(allSelectedRows, ['id', item]));

    if (selectedRows && selectedRows.length) {
      store.setSelectData(selectedRows);
    }
    store.changeShow(false);
  };

  render() {
    const {
      store: { getAppDetailById },
      intl: { formatMessage },
      show,
    } = this.props;
    const contentDom = (
      <Content
        className="c7n-region sidebar-content"
        code="release.addVersion"
        values={{ name: getAppDetailById && getAppDetailById.name }}
      >
        {this.getSidebarTable()}
      </Content>
    );
    return (
      <Sidebar
        okText={formatMessage({ id: 'release.addVersion.btn.confirm' })}
        cancelText={formatMessage({ id: 'cancel' })}
        visible={show}
        title={<FormattedMessage id="release.addVersion.header.title" />}
        onCancel={this.handleClose}
        onOk={this.handleAddVersion}
      >
        {contentDom}
      </Sidebar>
    );
  }
}

export default withRouter(injectIntl(VersionTable));
