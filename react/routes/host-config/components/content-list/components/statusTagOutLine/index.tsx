import React from 'react';

import { StatusTagOutLineProps, statusKindsMap } from '../../interface';

import './index.less';

const prefixCls = 'c7ncd-host-config';

const statusKindMap:statusKindsMap = {
  success: {
    text: '成功',
    bgColor: 'rgba(31, 194, 187, 0.12)',
    fontColor: 'rgba(31, 194, 187, 1)',
  },
  pending: {
    text: '失败',
    bgColor: 'rgba(247, 103, 118, 0.12)',
    fontColor: 'rgba(247, 103, 118, 1)',
  },
  failed: {
    text: '连接中',
    bgColor: 'rgba(77, 144, 254, 0.12)',
    fontColor: 'rgba(77, 144, 254, 1)',
  },
  default: {
    text: 'unknown',
    bgColor: 'rgba(216, 216, 216, 0.12)',
    fontColor: 'rgb(216, 216, 216)',
  },
};

const StatusTag:React.FC<StatusTagOutLineProps> = ({
  fontSize,
  status,
}) => (
  <span
    className={`${prefixCls}-statusTagOutLine`}
    style={{
      color: statusKindMap[status].fontColor,
      backgroundColor: statusKindMap[status].bgColor,
      fontSize: `${fontSize}px`,
    }}
  >
    {statusKindMap[status].text}
  </span>
);

StatusTag.defaultProps = {
  fontSize: 11,
  status: 'default',
};

export default StatusTag;
