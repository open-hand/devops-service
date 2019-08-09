import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { Action } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui/pro';
import toUpper from 'lodash/toUpper';
import StatusDot from '../../components/status-dot';
import PodCircle from '../../components/pod-circle';
import { useDeploymentStore } from '../../../stores';
import { useMainStore } from '../../stores';
import { useSidebarStore } from '../stores';

import './index.less';

const TreeItem = observer(({ record, search }) => {
  const {
    prefixCls,
    itemType: {
      ENV_ITEM,
      APP_ITEM,
      IST_ITEM,
      GROUP_ITEM,
      SERVICES_ITEM,
      INGRESS_ITEM,
      CERT_ITEM,
      MAP_ITEM,
      CIPHER_ITEM,
      CUSTOM_ITEM,
    },
    intl: { formatMessage },
  } = useDeploymentStore();
  const {
    podColor: {
      RUNNING_COLOR,
      PADDING_COLOR,
    },
  } = useMainStore();
  const { treeDs } = useSidebarStore();

  const type = record.get('itemType');
  const name = record.get('name');
  const isExpand = record.isExpanded;

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
      case GROUP_ITEM:
      case SERVICES_ITEM:
      case INGRESS_ITEM:
      case CERT_ITEM:
      case MAP_ITEM:
      case CIPHER_ITEM:
      case CUSTOM_ITEM: {
        const iconMappings = {
          [APP_ITEM]: 'widgets',
          [GROUP_ITEM]: 'folder_open',
          [SERVICES_ITEM]: 'router',
          [INGRESS_ITEM]: 'language',
          [CERT_ITEM]: 'class',
          [MAP_ITEM]: 'compare_arrows',
          [CIPHER_ITEM]: 'vpn_key',
          [CUSTOM_ITEM]: 'filter_b_and_w',
        };
        let iconType = iconMappings[type];

        if (type === GROUP_ITEM) {
          iconType = isExpand ? 'folder_open2' : 'folder_open';
        }

        prefix = <Icon type={iconType} />;
        break;
      }
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
  }, [APP_ITEM, CERT_ITEM, CIPHER_ITEM, CUSTOM_ITEM, ENV_ITEM, GROUP_ITEM, INGRESS_ITEM, IST_ITEM, MAP_ITEM, PADDING_COLOR, RUNNING_COLOR, SERVICES_ITEM, isExpand, record, type]);

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

  function freshMenu() {
    treeDs.query();
  }

  function getSuffix() {
    const istId = record.get('id');
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: () => freshMenu(istId),
    }, {
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: () => freshMenu(istId),
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }

  return <Fragment>
    {prefixIcon}
    <span className={`${prefixCls}-tree-text`}>
      {text}
    </span>
    {type === IST_ITEM && getSuffix()}
  </Fragment>;
});

TreeItem.propTypes = {
  record: PropTypes.shape({}),
};

TreeItem.defaultProps = {
  record: {},
};

export default TreeItem;
