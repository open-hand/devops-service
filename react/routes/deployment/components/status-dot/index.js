import React, { memo } from 'react';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import { Tooltip } from 'choerodon-ui/pro';

import './index.less';

const STATUS_OPERATING = 'operating';
const STATUS_RUNNING = 'running';
const STATUS_DISCONNECT = 'disconnect';

const StatusDot = memo(({ connect, synchronize, width }) => {
  /**
   *[connect: true, synchronize: true]   已连接 #0bc2a8
   *[connect: false, synchronize: true]  未连接 #ff9915
   *[synchronize: false]                 处理中 #4d90fe
   */
  let status;
  if (!synchronize) {
    status = STATUS_OPERATING;
  } else if (connect) {
    status = STATUS_RUNNING;
  } else {
    status = STATUS_DISCONNECT;
  }

  const styled = classnames({
    'c7ncd-status': true,
    [`c7ncd-status-${status}`]: true,
  });
  const dotWidth = width || '0.08rem';

  const dot = <i className={styled} style={{ width: dotWidth, height: dotWidth }} />;

  return status ? <Tooltip
    placement="top"
    title={<FormattedMessage id={status} />}
  >
    {dot}
  </Tooltip> : dot;
});

StatusDot.propTypes = {
  connect: PropTypes.bool.isRequired,
  synchronize: PropTypes.bool.isRequired,
  width: PropTypes.string,
};

export default StatusDot;
