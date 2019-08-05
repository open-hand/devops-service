import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Table, Modal, Tabs } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import TimePopover from '../../../components/timePopover';
import '../../main.scss';
import './index.scss';

const TabPane = Tabs.TabPane;
const { AppState } = stores;

@observer
class EditVersion extends Component {
  constructor(props) {
    const menu = AppState.currentMenuType;
    super(props);
    this.state = {
      name: props.match.params.name || '',
      id: props.match.params.id || '',
      projectId: menu.id,
      selectedRowKeys: [],
      key: '1',
    };
  }

  componentDidMount() {
    const { projectId } = this.state;
    const { EditVersionStore } = this.props;
    EditVersionStore.loadData({ projectId, id: this.state.id });
  }

  /**
   * 获取未发布版本
   * @returns {*}
   */
  getSidebarTable =() => {
    const { EditVersionStore } = this.props;
    const data = EditVersionStore.getUnReleaseData;
    const columns = [{
      title: <FormattedMessage id="deploy.ver" />,
      dataIndex: 'version',
    }, {
      title: <FormattedMessage id="app.createTime" />,
      render: (text, record) => <TimePopover content={record.creationDate} />,
    }];
    const rowSelection = {
      selectedRowKeys: this.state.selectedRowKeys || [],
      onChange: (selectedRowKeys, selectedRows) => {
        this.setState({ selectedRows, selectedRowKeys });
      },
    };
    return (<Table
      loading={EditVersionStore.loading}
      pagination={EditVersionStore.getUnPageInfo}
      rowSelection={rowSelection}
      columns={columns}
      dataSource={data}
      filterBarPlaceholder={this.props.intl.formatMessage({ id: 'filter' })}
      onChange={this.versionTableChange}
      rowKey={record => record.id}
    />);
  };

  /**
   * 获取已发布版本
   * @returns {*}
   */
  getPublishTable = () => {
    const { EditVersionStore } = this.props;
    const data = EditVersionStore.getReleaseData;
    const columns = [{
      title: <FormattedMessage id="deploy.ver" />,
      dataIndex: 'version',
    }, {
      title: <FormattedMessage id="app.createTime" />,
      render: (text, record) => <TimePopover content={record.creationDate} />,
    }, {
      title: <FormattedMessage id="release.editVersion.publishTime" />,
      render: (text, record) => <TimePopover content={record.updatedDate} />,
    }];
    return (<Table
      filterBarPlaceholder={this.props.intl.formatMessage({ id: 'filter' })}
      loading={EditVersionStore.loading}
      pagination={EditVersionStore.getPageInfo}
      columns={columns}
      dataSource={data}
      onChange={this.versionTableChange}
      rowKey={record => record.id}
    />);
  }

  /**
   * table app表格搜索
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   */
  versionTableChange =(pagination, filters, sorter, paras) => {
    const { EditVersionStore } = this.props;
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
    const page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
      // page = 1;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    EditVersionStore
      .loadData({
        page,
        size: pagination.pageSize,
        projectId: organizationId,
        sorter: sort,
        postData,
        key: this.state.key,
        id: this.state.id,
      });
  };

  /**
   * 切换tabs
   * @param value
   */
  changeTabs = (value) => {
    const { EditVersionStore } = this.props;
    this.setState({ key: value });
    EditVersionStore
      .loadData({ projectId: this.state.projectId, id: this.state.id, key: value });
  }

  /**
   * 返回上一级目录
   */
  handleBack =() => {
    const menu = AppState.currentMenuType;
    const { id, name, organizationId } = menu;
    this.props.history.push(`/devops/app-release/2?type=project&id=${id}&name=${name}&organizationId=${organizationId}`);
  }

  /**
   * 发布应用版本
   */
  handleOk = () => {
    const { selectedRows, id } = this.state;
    const { EditVersionStore } = this.props;
    this.setState({ submitting: true });
    EditVersionStore.updateData(this.state.projectId, id, selectedRows)
      .then((data) => {
        if (data) {
          this.setState({ submitting: false });
          this.handleBack();
        }
      }).catch((err) => {
        this.setState({ submitting: false });
        Choerodon.prompt(err.response.message);
      });
  }

  /**
   * 打开弹框
   */
  handleOpen = () => {
    this.setState({ visible: true });
  }

  /**
   * 关闭弹框
   */
  handleClose = () => {
    this.setState({ visible: false });
  }

  render() {
    const menu = AppState.currentMenuType;
    const { key, selectedRows } = this.state;
    return (
      <Page
        className="c7n-region"
        service={[
          'devops-service.application-market.queryAppVersionsInProject',
          'devops-service.application-market.updateVersions',
        ]}
      >
        <Header title={<FormattedMessage id="release.editVersion.header.title" />} backPath={`/devops/app-release/2?type=${menu.type}&id=${menu.id}&name=${menu.name}&organizationId=${menu.organizationId}`} />
        <Content code="release.editVersion" values={{ name: this.state.name }}>
          <Tabs defaultActiveKey={this.state.key || '1'} onChange={this.changeTabs}>
            <TabPane tab={<FormattedMessage id="release.editVersion.version.unpublish" />} key="1">
              <div className="version-table-wrap">
                {this.getSidebarTable()}
              </div>
            </TabPane>
            <TabPane tab={<FormattedMessage id="release.editVersion.version.publish" />} key="2">
              <div className="version-table-wrap">
                {this.getPublishTable()}
              </div>
            </TabPane>
          </Tabs>
          {key === '1' ? <React.Fragment>
            <div className="c7n-appRelease-hr" />
            <Permission service={['devops-service.application-market.updateVersions']}>
              <Button
                disabled={!(selectedRows && selectedRows.length)}
                className="release-button-margin"
                type="primary"
                funcType="raised"
                onClick={this.handleOpen}
              >{this.props.intl.formatMessage({ id: 'release.add.step.five.btn.confirm' })}</Button>
            </Permission>
            <Button funcType="raised" onClick={this.handleBack}>{this.props.intl.formatMessage({ id: 'cancel' })}</Button>
          </React.Fragment> : null}
        </Content>
        <Modal
          visible={this.state.visible}
          title={this.props.intl.formatMessage({ id: 'release.editVersion.modal.title' })}
          closable={false}
          footer={[
            <Button key="back" disabled={this.state.submitting} onClick={this.handleClose}>{this.props.intl.formatMessage({ id: 'cancel' })}</Button>,
            <Button key="submit" loading={this.state.submitting} type="primary" onClick={this.handleOk}>
              {this.props.intl.formatMessage({ id: 'release.add.step.five.btn.confirm' })}
            </Button>,
          ]}
        >
          <p>{this.props.intl.formatMessage({ id: 'release.editVersion.modal.content' })}</p>
        </Modal>
      </Page>
    );
  }
}

export default withRouter(injectIntl(EditVersion));
