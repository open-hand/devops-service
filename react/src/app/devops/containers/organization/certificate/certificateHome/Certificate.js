import React, { Component, Fragment } from "react";
import { observer, inject } from "mobx-react";
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from "react-intl";
import {
  Content,
  Header,
  Page,
  Permission,
} from "@choerodon/boot";
import { Button, Tooltip, Table, Modal } from "choerodon-ui";
import _ from "lodash";
import "../../../main.scss";
import "./Certificate.scss";
import CertificateCreate from "../certificateCreate";
import { HEIGHT } from '../../../../common/Constants';

@injectIntl
@withRouter
@inject('AppState')
@observer
class Certificate extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deleteCert: null,
      deleteStatus: false,
      removeDisplay: false,
      showType: '',
      id: null,
    };
  }

  componentDidMount() {
    this.loadCertData();
  }

  componentWillUnmount() {
    const { CertificateStore } = this.props;
    CertificateStore.setTableFilter({
      page: 0,
      pageSize: HEIGHT <= 900 ? 10 : 15,
      param: [],
      filters: {},
      postData: { searchParam: {}, param: "" },
      sorter: {
        field: "id",
        columnKey: "id",
        order: "descend",
      },
    });
  }

  loadCertData = () => {
    const {
      CertificateStore,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;
    const {
      page,
      pageSize,
      sorter,
      postData,
    } = CertificateStore.getTableFilter;
    CertificateStore.loadCertData(
      organizationId,
      page,
      pageSize,
      sorter,
      postData,
    );
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
    this.setState({ createSelectedRowKeys: [], createSelected: [], showType: '' });
    CertificateStore.setCert(null);
  };

  /**
   * 删除证书
   */
  handleDelete = () => {
    const {
      CertificateStore,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;
    const { deleteCert } = this.state;
    this.setState({ deleteStatus: true });
    CertificateStore.deleteCertById(organizationId, deleteCert)
      .then(data => {
        const { page, pageSize, sorter, postData } = CertificateStore.getTableFilter;
        if (data && data.failed) {
          this.setState({ deleteStatus: false });
          Choerodon.prompt(data.message);
        } else {
          CertificateStore.loadCertData(
            organizationId,
            page,
            pageSize,
            sorter,
            postData,
          );
          this.setState({ deleteStatus: false, removeDisplay: false });
        }
      })
      .catch(err => {
        this.setState({ deleteStatus: false });
        Choerodon.handleResponseError(err);
      });
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
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const {
      CertificateStore,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;
    const { current, pageSize } = pagination;
    const page = current - 1;
    const sort = _.isEmpty(sorter)
      ? {
        field: "id",
        columnKey: "id",
        order: "descend",
      }
      : sorter;
    const searchParam = {};
    let param = "";
    if (!_.isEmpty(filters)) {
      _.forEach(filters, (value, key) => {
        if (!_.isEmpty(value)) {
          searchParam[key === 'name' ? 'certName' : key] = [String(value)];
        }
      });
    }
    if (paras.length) {
      param = paras.toString();
    }
    const postData = {
      searchParam,
      param,
    };
    CertificateStore.setTableFilter({
      page,
      pageSize,
      filters,
      postData,
      sorter: sort,
      param: paras,
    });
    CertificateStore.loadCertData(organizationId, page, pageSize, sort, postData);
  };

  /**
   * 操作列
   * @param record
   * @param type
   * @param projectId
   * @param orgId
   */
  opColumn = (record, type, orgId) => {
    const { id, name } = record;
    return (
      <Fragment>
        <Permission
          service={["devops-service.org-certification.update"]}
          type={type}
          organizationId={orgId}
        >
          <Tooltip
            trigger="hover"
            placement="bottom"
            title={<FormattedMessage id="write" />}
          >
            <Button
              icon="mode_edit"
              shape="circle"
              size="small"
              funcType="flat"
              onClick={this.showSideBar.bind(this, 'edit', id)}
            />
          </Tooltip>
        </Permission>
        <Permission
          service={["devops-service.org-certification.deleteOrgCert"]}
          type={type}
          organizationId={orgId}
        >
          <Tooltip
            trigger="hover"
            placement="bottom"
            title={<FormattedMessage id="delete" />}
          >
            <Button
              icon="delete_forever"
              shape="circle"
              size="small"
              funcType="flat"
              onClick={this.openRemoveModal.bind(this, id, name)}
            />
          </Tooltip>
        </Permission>
      </Fragment>
    );
  };

  getCertTable = () => {
    const {
      intl: {formatMessage},
      CertificateStore,
      AppState: { currentMenuType: { organizationId: orgId, type } },
    } = this.props;
    const {
      filters,
      param,
    } = CertificateStore.getTableFilter;
    const {
      getCertLoading,
      getPageInfo,
      getCertData,
    } = CertificateStore;

    const columns = [
      {
        title: <FormattedMessage id="ctf.column.name"/>,
        key: "name",
        dataIndex: "name",
        filters: [],
        filteredValue: filters.name || [],
      },
      {
        title: <FormattedMessage id="ctf.column.ingress"/>,
        key: "domain",
        dataIndex: "domain",
      },
      {
        align: "right",
        width: 105,
        key: "action",
        render: record => this.opColumn(record, type, orgId),
      },
    ];
    return (
      <Table
        filterBarPlaceholder={formatMessage({id: "filter"})}
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

  render() {
    const {
      CertificateStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: { organizationId: orgId, type, name },
      },
    } = this.props;
    const { removeDisplay, deleteStatus, certName, showType, id } = this.state;

    return (
      <Page
        className="c7n-region c7n-certificate-wrapper"
        service={[
          "devops-service.org-certification.listOrgCert",
          "devops-service.org-certification.query",
          "devops-service.org-certification.pageProjects",
          "devops-service.org-certification.listCertProjects",
          "devops-service.org-certification.checkName",
          "devops-service.org-certification.create",
          "devops-service.org-certification.update",
          "devops-service.org-certification.deleteOrgCert",
        ]}
      >
        <Header title={<FormattedMessage id="certificate.head" />}>
          <Permission
            type={type}
            organizationId={orgId}
            service={["devops-service.org-certification.create"]}
          >
            <Button
              funcType="flat"
              onClick={this.showSideBar.bind(this, 'create')}
              icon="playlist_add"
            >
              <FormattedMessage id="ctf.create" />
            </Button>
          </Permission>
          <Permission
            type={type}
            organizationId={orgId}
            service={["devops-service.org-certification.listOrgCert"]}
          >
            <Button
              onClick={this.loadCertData}
              icon="refresh"
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Permission>
        </Header>
        <Content className="page-content" code="certificate" values={{ name }}>
          {this.getCertTable()}
        </Content>
        <Modal
          confirmLoading={deleteStatus}
          visible={removeDisplay}
          title={`${formatMessage({ id: "ctf.delete" })}“${certName}”`}
          closable={false}
          footer={[
            <Button
              key="back"
              onClick={this.closeRemoveModal}
              disabled={deleteStatus}
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
        </Modal>
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

export default Certificate;
