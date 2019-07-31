import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui/pro';
import StatusDot from '../components/status-dot';
import PodCircle from '../components/pod-circle';
import { getEnvInfo, getPodsInfo } from '../util';
import Store from '../stores';

const TreeItemIcon = React.memo(({ type, record }) => {
  const {
    podColor: {
      RUNNING_COLOR,
      PADDING_COLOR,
    },
    instanceView: {
      ENV_ITEM,
      APP_ITEM,
      IST_ITEM,
    },
  } = useContext(Store);

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

TreeItemIcon.propTypes = {
  type: PropTypes.string,
  record: PropTypes.shape({}),
};

TreeItemIcon.defaultProps = {
  record: {},
};

export default TreeItemIcon;
