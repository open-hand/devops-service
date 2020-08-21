import React from 'react';
import { Icon, Tooltip } from 'choerodon-ui';
import PropTypes from 'prop-types';
import './index.less';

const statusObj = {
  success: {
    icon: 'check_circle',
    text: '成功',
  },
  failed: {
    icon: 'cancel',
    text: '失败',
  },
  running: {
    icon: 'timelapse',
    text: '运行中',
  },
  canceled: {
    icon: 'cancle_b',
    text: '取消',
  },
  created: {
    icon: 'adjust',
    text: '未执行',
  },
  pending: {
    icon: 'pause_circle_filled',
    text: '准备中',
  },
  skipped: {
    icon: 'skipped_a',
    text: '已跳过',
  },
  not_audit: {
    icon: 'timelapse',
    text: '待审核',
  },
  stop: {
    icon: 'remove_circle',
    text: '已终止',
  },
};

const statusDot = (props) => {
  const { size, status, style, ...rest } = props;
  return (
    <Tooltip title={statusObj[status].text}>
      <Icon
        {...rest}
        type={statusObj[status].icon}
        style={{ fontSize: `${size}px`, ...style }}
        className={`c7n-piplineManage-detail-column-status-icon c7n-piplineManage-detail-column-status-icon-${status}`}
      />
    </Tooltip>
  );
};

statusDot.propTypes = {
  status: PropTypes.string.isRequired,
  size: PropTypes.number,
};
statusDot.defaultProps = {
  size: 12,
};

export default statusDot;
