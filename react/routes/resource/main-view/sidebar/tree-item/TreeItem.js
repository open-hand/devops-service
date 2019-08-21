import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import toUpper from 'lodash/toUpper';
import { Icon } from 'choerodon-ui/pro';
import StatusDot from '../../components/status-dot';
import InstanceItem from './InstanceItem';
import AppItem from './AppItem';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';

import './index.less';
import NetworkItem from './NetworkItem';
import CustomItem from './CustomItem';
import CertItem from './CertItem';
import IngressItem from './IngressItem';
import ConfigItem from './ConfigItem';

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

const EnvironmentItem = ({ name, connect, synchronize }) => {
  const getPrefix = useMemo(() => <StatusDot
    connect={connect}
    synchronize={synchronize}
    size="small"
  />, [connect, synchronize]);

  return <Fragment>
    {getPrefix}
    {name}
  </Fragment>;
};

EnvironmentItem.propTypes = {
  name: PropTypes.any,
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
};

const TreeItem = observer(({ record, search }) => {
  const {
    prefixCls,
    intlPrefix,
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
  } = useResourceStore();
  const { podColor } = useMainStore();

  const name = useMemo(() => {
    const itemName = record.get('name');
    return getName(itemName, search, prefixCls);
  }, [record, search]);

  function getIconItem(type) {
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

  function getItem() {
    const type = record.get('itemType');
    const isExpand = record.isExpanded;
    const isGroup = record.get('isGroup');
    const param = {
      record,
      name,
      intlPrefix,
    };

    if (isGroup) {
      return <Fragment>
        {isExpand ? <Icon type="folder_open2" /> : <Icon type="folder_open" />}
        {name}
      </Fragment>;
    }

    let treeItem;
    switch (type) {
      case ENV_ITEM: {
        treeItem = getEnvIcon();
        break;
      }
      case SERVICES_ITEM:
        treeItem = <NetworkItem {...param} />;
        break;
      case INGRESS_ITEM:
        treeItem = <IngressItem
          {...param}
        />;
        break;
      case CERT_ITEM:
        treeItem = <CertItem {...param} />;
        break;
      case MAP_ITEM:
        treeItem = <ConfigItem {...param} />;
        break;
      case CIPHER_ITEM:
        treeItem = <ConfigItem {...param} />;
        break;
      case CUSTOM_ITEM:
        treeItem = <CustomItem {...param} />;
        break;
      case IST_ITEM: {
        treeItem = <InstanceItem
          record={record}
          name={name}
          intlPrefix={intlPrefix}
          podColor={podColor}
        />;
        break;
      }
      case APP_ITEM:
        treeItem = <AppItem
          record={record}
          name={name}
          intlPrefix={intlPrefix}
        />;
        break;
      default:
        treeItem = null;
    }
    return treeItem;
  }

  return getItem();
});

TreeItem.propTypes = {
  record: PropTypes.shape({}),
  search: PropTypes.string,
};

TreeItem.defaultProps = {
  record: {},
};

export default TreeItem;
