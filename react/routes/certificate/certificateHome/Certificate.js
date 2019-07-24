import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Content,
  Header,
  Page,
  Permission,
} from '@choerodon/boot';
import { Button, Tooltip, Table, Modal } from 'choerodon-ui';
import _ from 'lodash';
import CertificateCreate from '../certificateCreate';

import '../../main.scss';
import './Certificate.scss';
import { handleCheckerProptError } from '../../../../utils';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Certificate extends Component {
  state = {
    deleteCert: null,
    deleteStatus: false,
    removeDisplay: false,
    showType: '',
    id: null,
  };

  componentDidMount() {
    this.loadCertData();
  }

  componentWillUnmount() {
    const { CertificateStore } = this.props;
    CertificateStore.initTableFilter();
  }

  loadCertData = () => {
    const {
      CertificateStore,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;

    CertificateStore.loadCertData(organizationId);
  };

  /**
   * 创建编辑证书侧边栏
   */
  showSideBar = (showType, id) => {
    this.setState({ showType, id });
  };

  /**
   * 关闭创建侧边栏
   */
  closeSideBar = () => {
    const { CertificateStore } = this.props;
    this.setState({
      createSelectedRowKeys: [],
      createSelected: [],
      showType: '',
    });
    CertificateStore.setCert(null);
  };

  /**
   * 删除证书
   */
  handleDelete = async () => {
    const {
      CertificateStore,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;
    const { deleteCert } = this.state;

    this.setState({ deleteStatus: true });

    const response = await CertificateStore.deleteCertById(organizationId, deleteCert)
      .catch(err => {
        this.setState({ deleteStatus: false });
        Choerodon.handleResponseError(err);
      });

    const result = handleCheckerProptError(response);
    if (result) {
      CertificateStore.setTableFilter({ page: 1 });
      CertificateStore.loadCertData(organizationId);
      this.setState({
        removeDisplay: false,
      });
    }

    this.setState({ deleteStatus: false });
  };

  /**
   * 显示删除确认框
   * @param id
   * @param certName
   */
  openRemoveModal = (id, certName) =>
    this.setState({
      removeDisplay: true,
      deleteCert: id,
      certName,
    });

  /**
   * 关闭删除确认框
   */
  closeRemoveModal = () => this.setState({ removeDisplay: false });

  /**
   * 表格筛选排序等
   * @param pagination
   * @param filters
   * @param sorter
   * @param param
   */
  tableChange = ({ current, pageSize }, filters, sorter, param) => {
    const {
      CertificateStore,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;

    const sort = _.isEmpty(sorter)
      ? {
        field: 'id',
        columnKey: 'id',
        order: 'descend',
      }
      : sorter;

    CertificateStore.setTableFilter({
      page: current,
      pageSize,
      postData: {
        searchParam: filters,
        param: param.toString(),
      },
      sorter: sort,
      filters,
      param,
    });
    CertificateStore.loadCertData(organizationId);
  };

  /**
   * 操作列
   */
  opColumn = ({ id, name }) => {
    const btnProps = {
      shape: 'circle',
      size: 'small',
      funcType: 'flat',
    };
    const tipProps = {
      trigger: 'hover',
      placement: 'bottom',
    };

    return (
      <Fragment>
        <Permission service={['devops-service.org-certification.update']}>
          <Tooltip
            {...tipProps}
            title={<FormattedMessage id="write" />}
          >
            <Button
              {...btnProps}
              icon="mode_edit"
              onClick={() => this.showSideBar('edit', id)}
            />
          </Tooltip>
        </Permission>
        <Permission service={['devops-service.org-certification.deleteOrgCert']}>
          <Tooltip
            {...tipProps}
            title={<FormattedMessage id="delete" />}
          >
            <Button
              {...btnProps}
              icon="delete_forever"
              onClick={() => this.openRemoveModal(id, name)}
            />
          </Tooltip>
        </Permission>
      </Fragment>
    );
  };

  get getCertTable() {
    const {
      intl: { formatMessage },
      CertificateStore: {
        getCertLoading,
        getPageInfo,
        getCertData,
        getTableFilter: {
          filters,
          param,
        },
      },
    } = this.props;

    const columns = [
      {
        title: <FormattedMessage id="ctf.column.name" />,
        key: 'name',
        dataIndex: 'name',
        filters: [],
        filteredValue: filters.name || [],
      },
      {
        title: <FormattedMessage id="ctf.column.ingress" />,
        key: 'domain',
        dataIndex: 'domain',
      },
      {
        align: 'right',
        width: 105,
        key: 'action',
        render: this.opColumn,
      },
    ];
    return (
      <Table
        noFilter
        filterBarPlaceholder={formatMessage({ id: 'filter' })}
        onChange={this.tableChange}
        loading={getCertLoading}
        pagination={getPageInfo}
        dataSource={getCertData}
        filters={param.slice()}
        columns={columns}
        rowKey={record => record.id}
      />
    );
  };

  get getDeleteModal() {
    const {
      intl: { formatMessage },
    } = this.props;
    const {
      removeDisplay,
      deleteStatus,
      certName,
    } = this.state;

    return (<Modal
      confirmLoading={deleteStatus}
      visible={removeDisplay}
      title={`${formatMessage({ id: 'ctf.delete' })}“${certName}”`}
      closable={false}
      footer={[
        <Button
          key="back"
          disabled={deleteStatus}
          onClick={this.closeRemoveModal}
        >
          <FormattedMessage id="cancel" />
        </Button>,
        <Button
          key="submit"
          loading={deleteStatus}
          type="danger"
          onClick={this.handleDelete}
        >
          <FormattedMessage id="delete" />
        </Button>,
      ]}
    >
      <div className="c7n-padding-top_8">
        <FormattedMessage id="ctf.delete.tooltip" />
      </div>
    </Modal>);
  }

  render() {
    const {
      CertificateStore,
      AppState: {
        currentMenuType: { name },
      },
    } = this.props;
    const {
      removeDisplay,
      showType,
      id,
    } = this.state;

    return (
      <Page
        className="c7n-region c7n-certificate-wrapper"
        service={[
          'devops-service.org-certification.listOrgCert',
          'devops-service.org-certification.query',
          'devops-service.org-certification.pageProjects',
          'devops-service.org-certification.listCertProjects',
          'devops-service.org-certification.checkName',
          'devops-service.org-certification.create',
          'devops-service.org-certification.update',
          'devops-service.org-certification.deleteOrgCert',
        ]}
      >
        <Header title={<FormattedMessage id="certificate.head" />}>
          <Permission service={['devops-service.org-certification.create']}>
            <Button
              funcType="flat"
              icon="playlist_add"
              onClick={() => this.showSideBar('create')}
            >
              <FormattedMessage id="ctf.create" />
            </Button>
          </Permission>
          <Permission service={['devops-service.org-certification.listOrgCert']}>
            <Button
              onClick={this.loadCertData}
              icon="refresh"
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Permission>
        </Header>
        <Content
          className="page-content"
          code="certificate"
          values={{ name }}
        >
          {this.getCertTable}
        </Content>
        {removeDisplay && this.getDeleteModal}
        {showType !== '' && (
          <CertificateCreate
            store={CertificateStore}
            onClose={this.closeSideBar}
            id={id}
            showType={showType}
          />
        )}
      </Page>
    );
  }
}
