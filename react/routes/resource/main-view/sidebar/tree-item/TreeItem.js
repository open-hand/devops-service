import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui/pro';
import TreeItemName from '../../../../../components/treeitem-name';
import EnvItem from '../../../../../components/env-item';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import InstanceItem from './InstanceItem';
import AppItem from './AppItem';
import NetworkItem from './NetworkItem';
import CustomItem from './CustomItem';
import CertItem from './CertItem';
import IngressItem from './IngressItem';
import ConfigItem from './ConfigItem';

const TreeItem = observer(({ record, search }) => {
  const {
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

  function getEnvItem(name) {
    const connect = record.get('connect');
    const synchronize = record.get('synchronize');

    return <EnvItem
      name={name}
      connect={connect}
      synchronize={synchronize}
    />;
  }

  function getNormalItem(type, param) {
    const mappings = {
      [SERVICES_ITEM]: () => <NetworkItem {...param} />,
      [INGRESS_ITEM]: () => <IngressItem {...param} />,
      [CERT_ITEM]: () => <CertItem {...param} />,
      [MAP_ITEM]: () => <ConfigItem {...param} />,
      [CIPHER_ITEM]: () => <ConfigItem {...param} />,
      [CUSTOM_ITEM]: () => <CustomItem {...param} />,
    };
    return mappings[type]();
  }

  function getItem() {
    const itemName = record.get('name');
    const name = <TreeItemName name={itemName} search={search} />;
    const isExpand = record.isExpanded;
    const isGroup = record.get('isGroup');
    if (isGroup) {
      return <Fragment>
        {isExpand ? <Icon type="folder_open2" /> : <Icon type="folder_open" />}
        {name}
      </Fragment>;
    }

    const type = record.get('itemType');
    const param = {
      record,
      name,
      intlPrefix,
    };
    let treeItem;
    switch (type) {
      case ENV_ITEM: {
        treeItem = getEnvItem(name);
        break;
      }
      case SERVICES_ITEM:
      case INGRESS_ITEM:
      case CERT_ITEM:
      case MAP_ITEM:
      case CIPHER_ITEM:
      case CUSTOM_ITEM:
        treeItem = getNormalItem(type, param);
        break;
      case IST_ITEM: {
        treeItem = <InstanceItem
          {...param}
          podColor={podColor}
        />;
        break;
      }
      case APP_ITEM:
        treeItem = <AppItem {...param} />;
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
