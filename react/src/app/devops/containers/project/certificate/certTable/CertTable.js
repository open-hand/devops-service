import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { injectIntl, FormattedMessage } from "react-intl";
import { Table, Icon, Button, Popover, Tooltip, Modal } from "choerodon-ui";
import { Permission, stores } from "@choerodon/boot";
import _ from "lodash";
import { getTimeLeft } from "../../../../utils";
import MouserOverWrapper from "../../../../components/MouseOverWrapper";
import StatusIcon from "../../../../components/StatusIcon";
import EnvFlag from "../../../../components/envFlag";
import "./CertTable.scss";
import Tips from "../../../../components/Tips";

const { AppState } = stores;
const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

@observer
class CertTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deleteCert: null,
      deleteStatus: false,
      removeDisplay: false,
    };
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setCertData([]);
    store.setPageInfo({
      current: 0,
      total: 0,
      pageSize: HEIGHT <= 900 ? 10 : 15,
    });
    store.setTableFilter({
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

  /**
   * 删除证书
   */
  handleDelete = () => {
    const { store, envId } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { deleteCert } = this.state;
    this.setState({ deleteStatus: true });
    store
      .deleteCertById(projectId, deleteCert)
      .then(data => {
        const { page, pageSize, sorter, postData } = store.getTableFilter;
        this.setState({ deleteStatus: false, removeDisplay: false });
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          store.loadCertData(
            true,
            projectId,
            page,
            pageSize,
            sorter,
            postData,
            envId
          );
        }
      })
      .catch(err => {
        this.setState({ deleteStatus: false });
        Choerodon.handleResponseError(err);
      });
  };

  /**
   * 表格筛选排序等
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { store, envId } = this.props;
    const { id: projectId } = AppState.currentMenuType;
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
          searchParam[key] = [String(value)];
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
    store.setTableFilter({
      page,
      pageSize,
      filters,
      postData,
      sorter: sort,
      param: paras,
    });
    store.loadCertData(true, projectId, page, pageSize, sort, postData, envId);
  };

  /**
   * 显示删除确认框
   * @param id
   */
  openRemoveModal = (id, certName) =>
    this.setState({
      removeDisplay: true,
      deleteCert: id,
      certName,
    });

  closeRemoveModal = () => this.setState({ removeDisplay: false });

  /**
   * 有效期
   * @param record
   * @returns {null}
   */
  validColumn = record => {
    const { validFrom, validUntil, commandStatus } = record;
    const {
      intl: { formatMessage },
    } = this.props;
    let msg = null;
    let content = null;
    if (validFrom && validUntil && commandStatus === "success") {
      content = (
        <div>
          <div>
            <FormattedMessage id="timeFrom" />：{validFrom}
          </div>
          <div>
            <FormattedMessage id="timeUntil" />：{validUntil}
          </div>
        </div>
      );
      const start = new Date(validFrom.replace(/-/g, "/")).getTime();
      const end = new Date(validUntil.replace(/-/g, "/")).getTime();
      const now = Date.now();
      if (now < start) {
        msg = <FormattedMessage id="notActive" />;
      } else if (now > end) {
        msg = <FormattedMessage id="expired" />;
      } else {
        msg = getTimeLeft(now, end);
      }
      return (
        <Popover
          content={content}
          getPopupContainer={triggerNode => triggerNode.parentNode}
          trigger="hover"
          placement="top"
        >
          <span>{msg}</span>
        </Popover>
      );
    }
    return null;
  };

  /**
   * 操作列
   * @param record
   * @param type
   * @param projectId
   * @param orgId
   */
  opColumn = (record, type, projectId, orgId) => {
    const { id, domains, certName, commandStatus } = record;
    const {
      intl: { formatMessage },
    } = this.props;
    const detail = {
      CommonName: [domains[0]],
      DNSNames: domains.slice(1),
    };
    const content = (
      <Fragment>
        {_.map(detail, (value, key) => {
          if (value.length) {
            return (
              <div className="c7n-overlay-content" key={value}>
                <p className="c7n-overlay-title">{key}</p>
                <div className="c7n-overlay-item">
                  {_.map(value, item => (
                    <p key={item} title={item} className="c7n-overlay-detail">
                      {item}
                    </p>
                  ))}
                </div>
              </div>
            );
          }
          return null;
        })}
      </Fragment>
    );
    return (
      <Fragment>
        <Popover
          overlayClassName="c7n-ctf-overlay"
          arrowPointAtCenter
          title={formatMessage({ id: "ctf.cert.detail" })}
          content={content}
          getPopupContainer={triggerNode => triggerNode.parentNode}
          trigger="hover"
          placement="bottomRight"
        >
          <Icon type="find_in_page" className="c7n-ctf-detail-icon" />
        </Popover>
        <Permission
          service={["devops-service.certification.delete"]}
          type={type}
          projectId={projectId}
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
              onClick={this.openRemoveModal.bind(this, id, certName)}
              disabled={commandStatus === 'operating'}
            />
          </Tooltip>
        </Permission>
      </Fragment>
    );
  };

  render() {
    const {
      intl: { formatMessage },
      store,
    } = this.props;
    const { removeDisplay, deleteStatus, certName } = this.state;
    const {
      filters,
      sorter: { columnKey, order },
      param,
    } = store.getTableFilter;
    const {
      type,
      id: projectId,
      organizationId: orgId,
      name,
    } = AppState.currentMenuType;

    const columns = [
      {
        title: <FormattedMessage id="ctf.column.name" />,
        key: "certName",
        dataIndex: "certName",
        filters: [],
        filteredValue: filters.certName || [],
        render: (text, record) => (
          <StatusIcon
            name={text}
            status={record.commandStatus || ""}
            error={record.error || ""}
          />
        ),
      },
      {
        title: <FormattedMessage id="ctf.column.ingress" />,
        key: "domains",
        dataIndex: "domains",
        filters: [],
        filteredValue: filters.domains || [],
        render: (text, record) => (
          <MouserOverWrapper text={text[0] || ""} width={0.25}>
            {text[0]}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="ctf.column.env" />,
        key: "envName",
        dataIndex: "envName",
        sorter: true,
        filters: [],
        sortOrder: columnKey === "envName" && order,
        filteredValue: filters.envName || [],
        render: (text, record) => (
          <EnvFlag status={record.envConnected} name={record.envName} />
        ),
      },
      {
        title: <Tips type="title" data="validDate" />,
        key: "valid",
        render: this.validColumn,
      },
      {
        align: "right",
        width: 100,
        key: "action",
        render: record => this.opColumn(record, type, projectId, orgId),
      },
    ];
    return (
      <Fragment>
        <Table
          filterBarPlaceholder={formatMessage({ id: "filter" })}
          onChange={this.tableChange}
          loading={store.getCertLoading}
          pagination={store.getPageInfo}
          dataSource={store.getCertData}
          filters={param.slice()}
          columns={columns}
          rowKey={record => record.id}
        />
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
      </Fragment>
    );
  }
}

export default injectIntl(CertTable);
