import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui/pro';
import toUpper from 'lodash/toUpper';
import StatusDot from '../../components/status-dot';
import PodCircle from '../../components/pod-circle';
import { useMainStore } from '../../stores';
import { useDeploymentStore } from '../../../stores';

import './index.less';

const TreeItem = ({ record, search }) => {
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
  } = useMainStore();
  const { prefixCls } = useDeploymentStore();
  const type = record.get('itemType');
  const name = record.get('name');

  const prefixIcon = useMemo(() => {
    let prefix;
    switch (type) {
      case ENV_ITEM: {
        const connect = record.get('connect');
        const synchronize = record.get('synchronize');

        prefix = <StatusDot
          connect={connect}
          synchronize={synchronize}
          size="small"
        />;
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
  }, [APP_ITEM, ENV_ITEM, IST_ITEM, PADDING_COLOR, RUNNING_COLOR, record, type]);

  const text = useMemo(() => {
    const index = toUpper(name).indexOf(toUpper(search));
    const beforeStr = name.substr(0, index);
    const currentStr = name.substr(index, search.length);
    const afterStr = name.substr(index + search.length);

    return index > -1 ? <Fragment>
      {beforeStr}
      <span className={`${prefixCls}-tree-text-highlight`}>{currentStr}</span>
      {afterStr}
    </Fragment> : name;
  }, [name, prefixCls, search]);

  return <Fragment>
    {prefixIcon}
    <span className={`${prefixCls}-tree-text`}>
      {text}
    </span>
  </Fragment>;
};

TreeItem.propTypes = {
  record: PropTypes.shape({}),
};

TreeItem.defaultProps = {
  record: {},
};

export default TreeItem;
