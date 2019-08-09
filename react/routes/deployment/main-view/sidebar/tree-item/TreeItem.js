import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import toUpper from 'lodash/toUpper';
import { Icon } from 'choerodon-ui/pro';
import InstanceItem from './InstanceItem';
import EnvironmentItem from './EnvironmentItem';
import AppItem from './AppItem';
import { useDeploymentStore } from '../../../stores';
import { useMainStore } from '../../stores';

import './index.less';

function getName(name, search, prefixCls) {
  const index = toUpper(name).indexOf(toUpper(search));
  const beforeStr = name.substr(0, index);
  const currentStr = name.substr(index, search.length);
  const afterStr = name.substr(index + search.length);

  return <span className={`${prefixCls}-tree-text`}>
    {index > -1 ? <Fragment>
      {beforeStr}
      <span className={`${prefixCls}-tree-text-highlight`}>{currentStr}</span>
      {afterStr}
    </Fragment> : name}
  </span>;
}

const TreeItem = observer(({ record, search }) => {
  const {
    prefixCls,
    itemType: {
      ENV_ITEM,
      APP_ITEM,
      IST_ITEM,
      SERVICES_ITEM,
      INGRESS_ITEM,
      CERT_ITEM,
      MAP_ITEM,
      CIPHER_ITEM,
      CUSTOM_ITEM,
    },
  } = useDeploymentStore();
  const { podColor } = useMainStore();

  const type = record.get('itemType');
  const isExpand = record.isExpanded;

  const name = useMemo(() => {
    const itemName = record.get('name');
    return getName(itemName, search, prefixCls);
  }, [prefixCls, record, search]);

  function getInstance() {
    const podRunningCount = record.get('podRunningCount');
    const podCount = record.get('podCount');
    const podUnlinkCount = podCount - podRunningCount;
    const istId = record.get('id');
    return istId ? <InstanceItem
      istId={istId}
      name={name}
      running={podRunningCount}
      unlink={podUnlinkCount}
      podColor={podColor}
    /> : null;
  }

  function getIconItem() {
    const iconMappings = {
      [SERVICES_ITEM]: 'router',
      [INGRESS_ITEM]: 'language',
      [CERT_ITEM]: 'class',
      [MAP_ITEM]: 'compare_arrows',
      [CIPHER_ITEM]: 'vpn_key',
      [CUSTOM_ITEM]: 'filter_b_and_w',
    };
    const iconType = iconMappings[type];
    return <Fragment>
      <Icon type={iconType} />
      {name}
    </Fragment>;
  }

  function getEnvIcon() {
    const connect = record.get('connect');
    const synchronize = record.get('synchronize');

    return <EnvironmentItem
      name={name}
      connect={connect}
      synchronize={synchronize}
    />;
  }

  function getItem(flag) {
    let treeItem;

    switch (flag) {
      case ENV_ITEM: {
        treeItem = getEnvIcon();
        break;
      }
      case SERVICES_ITEM:
      case INGRESS_ITEM:
      case CERT_ITEM:
      case MAP_ITEM:
      case CIPHER_ITEM:
      case CUSTOM_ITEM: {
        treeItem = getIconItem();
        break;
      }
      case IST_ITEM: {
        treeItem = getInstance();
        break;
      }
      case APP_ITEM:
        treeItem = <AppItem name={name} />;
        break;
      default:
        treeItem = null;
    }
    return treeItem;
  }

  return getItem(type);
});

TreeItem.propTypes = {
  record: PropTypes.shape({}),
  search: PropTypes.string,
};

TreeItem.defaultProps = {
  record: {},
};

export default TreeItem;
