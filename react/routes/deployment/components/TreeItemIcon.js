import React from 'react';
import { Icon } from 'choerodon-ui/pro';
import StatusDot from './status-dot';
import PodCircle from './pod-circle';

const RUNNING_COLOR = '#0bc2a8';
const PADDING_COLOR = '#fbb100';

export const ENV_ITEM = 'environment';
export const APP_ITEM = 'application';
export const IST_ITEM = 'instance';

export const TreeItemIcon = React.memo(({ type, record = {} }) => {
  let prefix;
  switch (type) {
    case ENV_ITEM: {
      const connect = record.get('connect');
      const synchronize = record.get('synchronize');

      prefix = <StatusDot connect={connect} synchronize={synchronize} />;
      break;
    }
    case APP_ITEM:
      prefix = <Icon type="widgets" />;
      break;
    case IST_ITEM: {
      const podRunningCount = record.get('podRunningCount');
      const podCount = record.get('podCount');
      const podUnlinkCount = podCount - podRunningCount;

      prefix = <PodCircle
        size="small"
        dataSource={[{
          name: 'running',
          value: podRunningCount,
          stroke: RUNNING_COLOR,
        }, {
          name: 'unlink',
          value: podUnlinkCount,
          stroke: PADDING_COLOR,
        }]}
      />;
      break;
    }
    default:
      prefix = null;
  }

  return prefix;
});
