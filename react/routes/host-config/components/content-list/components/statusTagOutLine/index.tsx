import { Tooltip } from 'choerodon-ui/pro/lib';
import React from 'react';

import { StatusTagOutLineProps, statusKindsMap } from '../../interface';

import './index.less';

const prefixCls = 'c7ncd-host-config';

const statusKindMap:statusKindsMap = {
  success: {
    text: '成功',
    bgColor: 'rgba(31, 194, 187, 0.12)',
    fontColor: 'rgba(31, 194, 187, 1)',
    hoverText: '测试连接：成功',
  },
  failed: {
    text: '失败',
    bgColor: 'rgba(247, 103, 118, 0.12)',
    fontColor: 'rgba(247, 103, 118, 1)',
    hoverText: '测试连接：失败',
  },
  operating: {
    text: '连接中',
    bgColor: 'rgba(77, 144, 254, 0.12)',
    fontColor: 'rgba(77, 144, 254, 1)',
    hoverText: '测试连接中',
  },
  occupied: {
    text: '占用中',
    bgColor: 'rgba(158, 173, 190, 0.16)',
    fontColor: 'rgba(15, 19, 88, 0.36)',
    hoverText: '连接被占用',
  },
  default: {
    text: 'unknown',
    bgColor: 'rgba(216, 216, 216, 0.12)',
    fontColor: 'rgb(216, 216, 216)',
    hoverText: '',
  },
};

const StatusTag:React.FC<StatusTagOutLineProps> = ({
  fontSize,
  status,
}) => (
  <Tooltip title={statusKindMap[status].hoverText} placement="topRight">
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
  </Tooltip>
);

StatusTag.defaultProps = {
  fontSize: 11,
  status: 'default',
};

export default StatusTag;
