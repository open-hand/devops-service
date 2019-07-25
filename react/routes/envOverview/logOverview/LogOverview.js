/* eslint-disable react/sort-comp */
import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from "react-intl";
import { Table, Form, Icon, Popover, Tooltip, Button, Modal } from "choerodon-ui";
import { stores, Permission } from "@choerodon/boot";
import TimePopover from "../../../components/timePopover";
import "../EnvOverview.scss";
import "../../main.scss";
import MouserOverWrapper from "../../../components/MouseOverWrapper";

const { AppState } = stores;

@observer
class LogOverview extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      submitting: false,
      showRetry: false,
    };
  }

  /**
   * table 操作
   * @param pagination
   */
  tableChange = pagination => {
    const { store, envId } = this.props;
    const { id } = AppState.currentMenuType;
    const page = pagination.current;
    store.loadLog(true, id, envId, page, pagination.pageSize);
  };

  /**
   * 打开或关闭重试弹窗
   */
  showRetry = (flag) => {
    this.setState({ showRetry: flag });
  };

  /**
   * 重试gitOps
   */
  handleRetry = () => {
    const { store, envId } = this.props;
    const { projectId } = AppState.currentMenuType;
    this.setState({ submitting: true });
    store.retry(projectId, envId)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          store.loadSync(projectId, envId);
          store.loadLog(true, projectId, envId);
          this.setState({ showRetry: false });
        }
        this.setState({ submitting: false });
      })
      .catch(err => {
        this.setState({ submitting: false });
        Choerodon.handleResponseError(err);
      });
  };

  render() {
    const { store, intl: { formatMessage } } = this.props;
    const { type, projectId, organizationId: orgId } = AppState.currentMenuType;
    const { showRetry, submitting } = this.state;
    const log = store.getLog;
    const sync = store.getSync;

    const columns = [
      {
        title: <FormattedMessage id="envoverview.logs.info" />,
        key: "error",
        width: "50%",
        render: record => (
          <MouserOverWrapper text={record.error || ""} width={0.5}>
            {record.error}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="envoverview.logs.file" />,
        key: "filePath",
        render: record => (
          <Fragment>
            <a
              href={record.fileUrl}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <span>{record.filePath}</span>
            </a>
          </Fragment>
        ),
      },
      {
        title: <FormattedMessage id="version.commit" />,
        key: "commit",
        render: record => (
          <Fragment>
            <a
              href={record.commitUrl}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <span>{record.commit && record.commit.slice(0, 8)}</span>
            </a>
          </Fragment>
        ),
      },
      {
        title: <FormattedMessage id="envoverview.logs.time" />,
        key: "errorTime",
        sorter: true,
        render: record => <TimePopover content={record.errorTime} />,
      },
    ];

    const content = (
      <Fragment>
        <p className="envow-popover-describe">
          <FormattedMessage id="envoverview.commit.desc" />
        </p>
        <h4 className="envow-popover-title">
          <FormattedMessage id="envoverview.gitlab" />
        </h4>
        <p className="envow-popover-desc">
          <FormattedMessage id="envoverview.commit.repo" />
        </p>
        <h4 className="envow-popover-title">
          <FormattedMessage id="envoverview.analysis" />
        </h4>
        <p className="envow-popover-desc">
          <FormattedMessage id="envoverview.commit.anal" />
        </p>
        <h4 className="envow-popover-title">
          <FormattedMessage id="envoverview.agent" />
        </h4>
        <p className="envow-popover-desc">
          <FormattedMessage id="envoverview.commit.carr" />
        </p>
      </Fragment>
    );

    const tableLocale = {
      emptyText: formatMessage({ id: "envoverview.log.table" }),
    };

    return (
      <div>
        <div className="c7n-envow-sync-wrap">
          <div className="c7n-envow-sync-title">
            <span className="envow-sync-text">
              <FormattedMessage id="envoverview.commit.sync" />
            </span>
            <Popover
              overlayClassName="c7n-envow-sync-popover"
              placement="bottomLeft"
              content={content}
              trigger="hover"
              arrowPointAtCenter
            >
              <Icon type="help" className="c7n-envow-sync-icon" />
            </Popover>
          </div>
          <div className="c7n-envow-sync-line">
            <div className="c7n-envow-sync-card">
              <div className="c7n-envow-sync-step">
                <FormattedMessage id="envoverview.gitlab" />
              </div>
              <div className="c7n-envow-sync-commit">
                <a
                  href={sync && `${sync.commitUrl}${sync.sagaSyncCommit}`}
                  target="_blank"
                  rel="nofollow me noopener noreferrer"
                >
                  {sync &&
                    (sync.sagaSyncCommit
                      ? sync.sagaSyncCommit.slice(0, 8)
                      : null)}
                </a>
              </div>
            </div>
            <div className="c7n-envow-sync-arrow c7n-envow-sync-retry">
              <Permission
                service={['devops-service.devops-environment.queryByCode']}
                organizationId={orgId}
                projectId={projectId}
                type={type}
              >
                <Tooltip title={<FormattedMessage id="envoverview.log.retry.title" />}>
                  <Button
                    shape='circle'
                    icon='replay'
                    type='primary'
                    onClick={this.showRetry.bind(this, true)}
                  />
                </Tooltip>
              </Permission>
              <div className="c7n-envow-sync-arrow-detail">→</div>
            </div>
            <div className="c7n-envow-sync-card">
              <div className="c7n-envow-sync-step">
                <FormattedMessage id="envoverview.analysis" />
              </div>
              <div className="c7n-envow-sync-commit">
                <a
                  href={sync && `${sync.commitUrl}${sync.devopsSyncCommit}`}
                  target="_blank"
                  rel="nofollow me noopener noreferrer"
                >
                  {sync &&
                    (sync.devopsSyncCommit
                      ? sync.devopsSyncCommit.slice(0, 8)
                      : null)}
                </a>
              </div>
            </div>
            <div className="c7n-envow-sync-arrow">
              <div className="c7n-envow-sync-arrow-detail">→</div>
            </div>
            <div className="c7n-envow-sync-card">
              <div className="c7n-envow-sync-step">
                <FormattedMessage id="envoverview.agent" />
              </div>
              <div className="c7n-envow-sync-commit">
                <a
                  href={sync && `${sync.commitUrl}${sync.agentSyncCommit}`}
                  target="_blank"
                  rel="nofollow me noopener noreferrer"
                >
                  {sync &&
                    (sync.agentSyncCommit
                      ? sync.agentSyncCommit.slice(0, 8)
                      : null)}
                </a>
              </div>
            </div>
          </div>
        </div>
        <div className="c7n-envow-sync-title">
          <FormattedMessage id="envoverview.logs.err" />
        </div>
        <Table
          filterBar={false}
          locale={tableLocale}
          loading={store.isLoading}
          pagination={store.getLogPageInfo}
          columns={columns}
          onChange={this.tableChange}
          dataSource={log}
          rowKey={record => record.id}
        />
        {showRetry && (
          <Modal
            confirmLoading={submitting}
            visible={showRetry}
            title={`${formatMessage({ id: 'envoverview.log.retry.title' })}`}
            closable={false}
            onOk={this.handleRetry}
            onCancel={this.showRetry.bind(this, false)}
          >
            <div className="c7n-padding-top_8">
              <FormattedMessage id={`envoverview.log.retry.des`} />
            </div>
          </Modal>
        )}
      </div>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(LogOverview)));
