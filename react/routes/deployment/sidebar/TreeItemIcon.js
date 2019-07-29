import React from 'react/index';
import { Icon } from 'choerodon-ui/pro';
import StatusDot from '../components/status-dot';
import PodCircle from '../components/pod-circle';
import { ENV_ITEM, APP_ITEM, IST_ITEM, PADDING_COLOR, RUNNING_COLOR } from '../Constants';
import { getEnvInfo, getPodsInfo } from '../util';

const TreeItemIcon = React.memo(({ type, record = {} }) => {
  let prefix;
  switch (type) {
    case ENV_ITEM: {
      const { connect, synchronize } = getEnvInfo(record);

      prefix = <StatusDot connect={connect} synchronize={synchronize} />;
      break;
    }
    case APP_ITEM:
      prefix = <Icon type="widgets" />;
      break;
    case IST_ITEM: {
      const { podUnlinkCount, podRunningCount } = getPodsInfo(record);

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

export default TreeItemIcon;
