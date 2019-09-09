import React, { useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Permission } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import { Tooltip, Button, Modal } from 'choerodon-ui/pro';
import { useEnvironmentStore } from '../stores';
import { useResourceStore } from '../../../../stores';

const SyncSituation = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: {
      getSelectedMenu: {
        synchronize,
      },
    },
  } = useResourceStore();
  const {
    intl: { formatMessage },
    gitopsLogDs,
    gitopsSyncDs,
    retryDs,
  } = useEnvironmentStore();

  /**
   * 打开重试弹窗
   */
  function showRetry() {
    Modal.open({
      key: 'retry',
      title: formatMessage({ id: `${intlPrefix}.environment.retry` }),
      children: <span>{formatMessage({ id: `${intlPrefix}.environment.retry.des` })}</span>,
      onOk: handleRetry,
    });
  }

  function refresh() {
    gitopsSyncDs.query();
    gitopsLogDs.query();
  }

  /**
   * 重试gitOps
   */
  async function handleRetry() {
    try {
      if ((await retryDs.query()) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  const getDetail = useMemo(() => {
    const record = gitopsSyncDs.current;
    if (record) {
      const commitUrl = record.get('commitUrl');
      const sagaSyncCommit = record.get('sagaSyncCommit');
      const devopsSyncCommit = record.get('devopsSyncCommit');
      const agentSyncCommit = record.get('agentSyncCommit');

      return (
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
              service={['devops-service.devops-environment.retryByGitOps']}
            >
              <Tooltip title={<FormattedMessage id={`${intlPrefix}.environment.retry`} />}>
                <Button
                  disabled={!synchronize}
                  icon="replay"
                  color="primary"
                  funcType="flat"
                  onClick={showRetry}
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
      );
    }
    return null;
  }, [gitopsSyncDs.data, showRetry]);

  return (
    <div className={`${prefixCls}-environment-sync-detail`}>
      {getDetail}
    </div>
  );
});

export default SyncSituation;
