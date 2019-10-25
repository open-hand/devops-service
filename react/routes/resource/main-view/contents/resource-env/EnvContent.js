import React, { useEffect, useMemo } from 'react';
import PropTypes from 'prop-types';
import { runInAction } from 'mobx';
import { observer } from 'mobx-react-lite';
import { Icon, Tooltip, Spin } from 'choerodon-ui';
import PageTitle from '../../../../../components/page-title';
import EnvItem from '../../../../../components/env-item';
import Modals from './modals';
import { useResourceStore } from '../../../stores';
import { useREStore } from './stores';
import DosageTable from './DosageTable';

import './index.less';
import openWarnModal from '../../../../../utils/openWarnModal';

function countDisplay(count, max) {
  return count > max ? <Tooltip title={count}>{`${max}+`}</Tooltip> : count;
}

function ItemNumberByStatus({ code, count, name, prefixCls }) {
  return (
    <div className={`${prefixCls}-re-grid-right-item`}>
      <div className={`${prefixCls}-re-status ${prefixCls}-re-status_${code}`}>
        {countDisplay(count, 99)}
      </div>
      <div className={`${prefixCls}-re-grid-right-text`}>
        <span>{name}</span>
      </div>
    </div>
  );
}

ItemNumberByStatus.propTypes = {
  code: PropTypes.string,
  count: PropTypes.number,
  name: PropTypes.string,
  prefixCls: PropTypes.string,
};

function ItemNumberByResource({ code, count, name, prefixCls }) {
  const iconMappings = {
    instanceCount: 'instance_outline',
    serviceCount: 'router',
    ingressCount: 'language',
    certificationCount: 'class',
    configMapCount: 'compare_arrows',
    secretCount: 'vpn_key',
  };
  return (
    <div className={`${prefixCls}-re-grid-left-item`}>
      <Icon type={iconMappings[code]} className={`${prefixCls}-re-grid-left-icon`} />
      <span className={`${prefixCls}-re-grid-left-number`}>{countDisplay(count, 99)}</span>
      <span className={`${prefixCls}-re-grid-left-name`}>{name}</span>
    </div>
  );
}

ItemNumberByResource.propTypes = {
  code: PropTypes.string,
  count: PropTypes.number,
  name: PropTypes.string,
  prefixCls: PropTypes.string,
};

const Content = observer(() => {
  const statusCount = useMemo(() => (['runningInstanceCount', 'operatingInstanceCount', 'stoppedInstanceCount', 'failedInstanceCount']), []);
  const resourceCount = useMemo(() => ([
    'instanceCount',
    'serviceCount',
    'ingressCount',
    'certificationCount',
    'configMapCount',
    'secretCount',
  ]), []);
  const {
    prefixCls,
    intlPrefix,
    intl: { formatMessage },
    treeDs,
    resourceStore,
  } = useResourceStore();
  const {
    baseInfoDs,
    resourceCountDs,
  } = useREStore();

  function getCounts(type) {
    const record = resourceCountDs.current;

    if (type === 'status') {
      return statusCount.map((item) => {
        const count = record ? record.get(item) : 0;
        const name = formatMessage({ id: `${intlPrefix}.status.${item}` });
        return <ItemNumberByStatus
          key={item}
          code={item}
          name={name}
          count={count}
          prefixCls={prefixCls}
        />;
      });
    }
    return resourceCount.map((item) => {
      const count = record ? record.get(item) : 0;
      const name = formatMessage({ id: `${intlPrefix}.resource.${item}` });
      return <ItemNumberByResource
        key={item}
        code={item}
        name={name}
        count={count}
        prefixCls={prefixCls}
      />;
    });
  }

  function refresh() {
    treeDs.query();
  }

  function getCurrent() {
    const record = baseInfoDs.current;
    if (record) {
      const id = record.get('id');
      const name = record.get('name');
      const active = record.get('active');
      const connect = record.get('connect');
      return { id, name, active, connect };
    }
    return null;
  }

  useEffect(() => {
    const currentBase = getCurrent();
    if (currentBase) {
      const { id, name, active, connect } = currentBase;
      const menuItem = treeDs.find((item) => item.get('key') === String(id));
      if (menuItem) {
        // 清除已经停用的环境
        if (!active) {
          openWarnModal(refresh, formatMessage);
        } else if ((menuItem.get('connect') !== connect
          || menuItem.get('name') !== name)) {
          runInAction(() => {
            menuItem.set({ name, connect });
            resourceStore.setSelectedMenu({
              ...resourceStore.getSelectedMenu,
              name,
              connect,
            });
          });
        }
      }
    }
  }, [baseInfoDs.current]);

  function getTitle() {
    const record = baseInfoDs.current;
    if (record) {
      const name = record.get('name');
      const connect = record.get('connect');

      return <EnvItem isTitle connect={connect} name={name} />;
    }
    return null;
  }

  function getFallBack() {
    const {
      name,
      connect,
    } = resourceStore.getSelectedMenu;
    return <EnvItem isTitle name={name} connect={connect} />;
  }

  return (
    <div className={`${prefixCls}-re`}>
      <PageTitle content={getTitle()} fallback={getFallBack()} />
      <Spin spinning={resourceCountDs.status === 'loading'}>
        <div className={`${prefixCls}-re-card-wrap`}>
          <div className={`${prefixCls}-re-card ${prefixCls}-re-card_left`}>
            <div className={`${prefixCls}-re-card-title`}>{formatMessage({ id: `${intlPrefix}.resource.deploy` })}</div>
            <div className={`${prefixCls}-re-grid-left`}>
              {getCounts()}
            </div>
          </div>
          <div className={`${prefixCls}-re-card ${prefixCls}-re-card_right`}>
            <div className={`${prefixCls}-re-card-title`}>{formatMessage({ id: `${intlPrefix}.instance.status` })}</div>
            <div className={`${prefixCls}-re-grid-right`}>{getCounts('status')}</div>
          </div>
        </div>
      </Spin>
      <DosageTable />
      <Modals />
    </div>
  );
});

export default Content;
