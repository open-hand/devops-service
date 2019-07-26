import React, { Component, Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react';
import { Permission } from '@choerodon/boot';
import {
  Tooltip,
  Button,
  Icon,
  Modal,
} from 'choerodon-ui/pro';
import { Popover } from 'choerodon-ui';
import Store from '../../../../stores';
import { handlePromptError } from '../../../../../../utils';

@observer
export default class SyncSituation extends Component {
  static contextType = Store;

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: {
          id,
        },
      },
      store: {
        getPreviewData: {
          id: envId,
        },
      },
      EnvLogStore,
    } = this.context;
    EnvLogStore.loadSync(id, envId);
  }

  /**
   * 打开重试弹窗
   */
  showRetry = () => {
    const {
      intlPrefix,
      intl,
    } = this.context;
    Modal.open({
      key: 'retry',
      title: intl.formatMessage({ id: `${intlPrefix}.environment.retry` }),
      children: <span>{intl.formatMessage({ id: `${intlPrefix}.environment.retry.des` })}</span>,
      onOk: this.handleRetry,
    });
  };

  /**
   * 重试gitOps
   */
  handleRetry = async () => {
    const {
      AppState: {
        currentMenuType: {
          id,
        },
      },
      store: {
        getPreviewData: {
          id: envId,
        },
      },
      EnvLogStore,
    } = this.context;
    const { loadData } = this.props;
    try {
      const data = await EnvLogStore.retry(id, envId);
      if (handlePromptError(data, false)) {
        EnvLogStore.loadSync(id, envId);
        loadData();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  };

  render() {
    const {
      prefixCls,
      intlPrefix,
      EnvLogStore,
      intl,
    } = this.context;
    const content = (
      <Fragment>
        <p className="log-help-desc">
          <FormattedMessage id={`${intlPrefix}.environment.help`} />
        </p>
        <h4 className="log-help-title">
          <FormattedMessage id={`${intlPrefix}.environment.config`} />
        </h4>
        <p className="log-help-desc">
          <FormattedMessage id={`${intlPrefix}.environment.config.des`} />
        </p>
        <h4 className="log-help-title">
          <FormattedMessage id={`${intlPrefix}.environment.parsed`} />
        </h4>
        <p className="log-help-desc">
          <FormattedMessage id={`${intlPrefix}.environment.parsed.des`} />
        </p>
        <h4 className="log-help-title">
          <FormattedMessage id={`${intlPrefix}.environment.executed`} />
        </h4>
        <p className="log-help-desc">
          <FormattedMessage id={`${intlPrefix}.environment.executed.des`} />
        </p>
      </Fragment>
    );
    const {
      commitUrl,
      sagaSyncCommit,
      devopsSyncCommit,
      agentSyncCommit,
    } = EnvLogStore.getSync || {};
    return (
      <div className={`${prefixCls}-environment-sync-detail`}>
        <div className="log-sync-title">
          <span className="log-sync-title-text">
            {intl.formatMessage({ id: `${intlPrefix}.environment.tabs.sync` })}
          </span>
          <Popover
            overlayClassName={`${prefixCls}-environment-sync-help`}
            placement="topLeft"
            content={content}
            arrowPointAtCenter
            theme="light"
          >
            <Icon type="help" className="log-sync-title-icon" />
          </Popover>
        </div>
        <div className="log-sync-line">
          <div className="log-sync-line-card">
            <div className="log-sync-line-card-title">
              <FormattedMessage id={`${intlPrefix}.environment.gitlab`} />
            </div>
            <div className="log-sync-line-card-commit">
              <a
                href={`${commitUrl}${sagaSyncCommit}`}
                target="_blank"
                rel="nofollow me noopener noreferrer"
              >
                {sagaSyncCommit ? sagaSyncCommit.slice(0, 8) : null}
              </a>
            </div>
          </div>
          <div className="log-sync-line-arrow log-sync-line-retry">
            <Permission
              service={['devops-service.devops-environment.queryByCode']}
            >
              <Tooltip title={<FormattedMessage id={`${intlPrefix}.environment.retry`} />}>
                <Button
                  icon="replay"
                  color="blue"
                  funcType="flat"
                  onClick={this.showRetry}
                />
              </Tooltip>
            </Permission>
            <div className="c7n-log-arrow-detail">→</div>
          </div>
          <div className="log-sync-line-card">
            <div className="log-sync-line-card-title">
              <FormattedMessage id={`${intlPrefix}.environment.analysis`} />
            </div>
            <div className="log-sync-line-card-commit">
              <a
                href={`${commitUrl}${devopsSyncCommit}`}
                target="_blank"
                rel="nofollow me noopener noreferrer"
              >
                {devopsSyncCommit ? devopsSyncCommit.slice(0, 8) : null}
              </a>
            </div>
          </div>
          <div className="log-sync-line-arrow">
            <div className="c7n-log-arrow-detail">→</div>
          </div>
          <div className="log-sync-line-card">
            <div className="log-sync-line-card-title">
              <FormattedMessage id={`${intlPrefix}.environment.agent`} />
            </div>
            <div className="log-sync-line-card-commit">
              <a
                href={`${commitUrl}${agentSyncCommit}`}
                target="_blank"
                rel="nofollow me noopener noreferrer"
              >
                {agentSyncCommit ? agentSyncCommit.slice(0, 8) : null}
              </a>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
