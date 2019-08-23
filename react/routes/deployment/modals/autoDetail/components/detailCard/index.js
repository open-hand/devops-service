import React, { PureComponent, Fragment } from 'react';
import PropTypes from 'prop-types';
import { withRouter, Link } from 'react-router-dom';
import _ from 'lodash';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Collapse, Icon, Tooltip } from 'choerodon-ui';
import { STATUS_ICON } from '../Constants';
import StatusIcon from '../../../../../../components/StatusIcon';

import './index.less';

const { Panel } = Collapse;
const INSTANCE_DELETE_STATUS = 'deleted';

@injectIntl
@withRouter
export default class DetailCard extends PureComponent {
  static propTypes = {
    isParallel: PropTypes.number,
    tasks: PropTypes.array,
  };

  render() {
    const {
      isParallel,
      tasks,
      intl: { formatMessage },
      location: {
        search,
        state: recordRouteState,
      },
      match: {
        params: {
          rId,
        },
      },
    } = this.props;
    const executeType = ['serial', 'parallel'];
    const mode = ['orSign', 'sign'];

    const task = _.map(tasks,
      ({
        id,
        name,
        status,
        taskType,
        isCountersigned,
        userDTOList,
        appName,
        envName,
        version,
        instanceStatus,
        applicationId,
        envId,
        instanceId,
        instanceName,
        envPermission,
      }) => {
        const panelHead = (<div className="c7ncd-pipeline-panel-title">
          <Tooltip
            title={name}
            placement="top"
          >
            <span className="c7ncd-pipeline-panel-name">
              【{formatMessage({ id: `pipeline.mode.${taskType}` })}】
              <span className="c7ncd-stage-name-light">{name}</span>
            </span>
          </Tooltip>
          <Icon type={STATUS_ICON[status]} className={`task-status_${status}`} />
        </div>);

        const auditUsers = [];
        const allAuditUsers = [];

        _.forEach(userDTOList, ({ audit, realName, loginName }) => {
          if (audit) {
            auditUsers.push(realName || loginName);
          } else {
            allAuditUsers.push(realName || loginName);
          }
        });

        let instanceNode = null;

        if (instanceName) {
          if ((instanceStatus && instanceStatus === INSTANCE_DELETE_STATUS) || !envPermission) {
            instanceNode = <StatusIcon name={instanceName} status={instanceStatus} width="110px" />;
          } else {
            instanceNode = <Link
              to={{
                pathname: '/devops/instance',
                search,
                state: {
                  ...recordRouteState,
                  instanceId,
                  recordId: rId,
                  applicationId,
                  envId,
                },
              }}
            >
              <StatusIcon name={instanceName} status={instanceStatus} width="110px" />
            </Link>;
          }
        } else {
          instanceNode = formatMessage({ id: 'null' });
        }

        const expandRow = {
          manual: () => (<Fragment>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'pipeline.detail.mode' })}</span>
              {formatMessage({ id: `pipeline.audit.${mode[isCountersigned]}` })}
            </div>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'pipeline.detail.users.all' })}</span>
              {allAuditUsers.length ? allAuditUsers.join('，') : formatMessage({ id: 'null' })}
            </div>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'pipeline.detail.users.audit' })}</span>
              {auditUsers.length ? auditUsers.join('，') : formatMessage({ id: 'null' })}
            </div>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'pipeline.detail.result' })}</span>
              {formatMessage({ id: `pipeline.result.${status}` })}
            </div>
          </Fragment>),
          auto: () => (<Fragment>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'app' })}：</span>
              {appName}
            </div>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'version' })}：</span>
              {version || formatMessage({ id: 'null' })}
            </div>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label">{formatMessage({ id: 'environment' })}：</span>
              {envName}
            </div>
            <div className="c7ncd-pipeline-task">
              <span className="c7ncd-pipeline-task-label c7ncd-pipeline-middle">{formatMessage({ id: 'pipeline.detail.instance' })}</span>
              {instanceNode}
            </div>
          </Fragment>),
        };
        return <Panel key={id} className="c7ncd-pipeline-panel" header={panelHead}>
          {expandRow[taskType] ? expandRow[taskType]() : null}
        </Panel>;
      });

    return (<div className="c7ncd-pipeline-card">
      <div className="c7ncd-task-top">{formatMessage({ id: 'pipeline.task.settings' })} - <FormattedMessage
        id={`pipeline.task.${executeType[isParallel]}`}
      /></div>
      <h4 className="c7ncd-task-header">{formatMessage({ id: 'pipeline.task.list' })}</h4>
      {task.length
        ? <Collapse className="c7ncd-pipeline-collapse" bordered={false}>
          {task}
        </Collapse>
        : <div className="c7ncd-pipeline-task-empty">
          <FormattedMessage id="pipeline.task.empty" />
        </div>}
    </div>);
  }
}
