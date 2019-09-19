import React, { memo } from 'react';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import { Tooltip } from 'choerodon-ui/pro';

import './index.less';

export const statusMappings = {
  STOPPED: 'stopped',
  OPERATING: 'operating',
  FAILED: 'failed',
  RUNNING: 'running',
  DISCONNECTED: 'disconnect',
};

/**
 * @param connect
 * @param synchronize
 * @param active
 * @param failed
 * @returns {string}
 *
 * 四个字段分成三个层次，组合判断状态
 *
 *   - synchronize 与 failed 是集群创建层面：[false, -] 处理中，[true, true] 创建成功，[true, false] 创建失败
 *   - connect 环境与集群连接层面
 *   - active 环境本身层面，表示环境自身停启用
 *
 * 判断逻辑
 *
 *   1. 是否在处理中（创建中，删除中，变更中）
 *   2. 是否创建失败
 *   3. 是否为停用 （停用的环境不用管连接未连接）
 *   4. 是否已连接集群
 *
 */
export function getEnvStatus({ connect, synchronize, active, failed }) {
  if (!synchronize) {
    return statusMappings.OPERATING;
  } else if (failed) {
    return statusMappings.FAILED;
  } else if (!active) {
    return statusMappings.STOPPED;
  } else if (connect) {
    return statusMappings.RUNNING;
  } else {
    return statusMappings.DISCONNECTED;
  }
}

const StatusDot = memo(({ connect, synchronize, active, size, getStatus, failed }) => {
  let status;
  let text;
  if (getStatus && typeof getStatus === 'function') {
    [status, text] = getStatus();
  } else {
    status = getEnvStatus({ connect, synchronize, active, failed });
  }

  const styled = classnames({
    'c7ncd-env-status': true,
    [`c7ncd-env-status-${status}`]: true,
    [`c7ncd-env-status-${size}`]: true,
  });
  const dot = <i className={styled} />;
  return status ? <Tooltip
    placement="top"
    title={<FormattedMessage id={text || status} />}
  >
    {dot}
  </Tooltip> : dot;
});

StatusDot.propTypes = {
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
  active: PropTypes.bool,
  failed: PropTypes.bool,
  size: PropTypes.string,
  getStatus: PropTypes.func,
};

StatusDot.defaultProps = {
  size: 'normal',
  active: true,
  failed: false,
};

export default StatusDot;
