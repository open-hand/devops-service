import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { Icon, Tooltip } from 'choerodon-ui';
import StatusDot from '../../components/status-dot';
import PrefixTitle from '../../components/prefix-title';
import Modals from './modals';
import { useResourceStore } from '../../../stores';
import { useREStore } from './stores';

import './index.less';

function countDisplay(count, max) {
  return count > max ? <Tooltip title={count}>{`${max}+`}</Tooltip> : count;
}

function ItemNumberByStatus({ code, count, name, prefixCls }) {
  return (
    <div className={`${prefixCls}-re-item`}>
      <div className={`${prefixCls}-re-status ${prefixCls}-re-status_${code}`}>
        {countDisplay(count, 99)}
      </div>
      <div className={`${prefixCls}-re-text`}>
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
    instanceCount: 'router',
    serviceCount: 'language',
    ingressCount: 'class',
    certificationCount: 'compare_arrows',
    configMapCount: 'vpn_key',
    secretCount: 'filter_b_and_w',
  };
  return (
    <div className={`${prefixCls}-re-item-static`}>
      <Icon type={iconMappings[code]} className={`${prefixCls}-re-item-static-icon`} />
      <span className={`${prefixCls}-re-item-static-number`}>{countDisplay(count, 999)}</span>
      <span className={`${prefixCls}-re-item-static-name`}>{name}</span>
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
        code={item}
        name={name}
        count={count}
        prefixCls={prefixCls}
      />;
    });
  }

  const title = useMemo(() => {
    const record = baseInfoDs.current;
    if (record) {
      const name = record.get('name');
      const connect = record.get('connect');
      const synchronize = record.get('synchronize');

      return <Fragment>
        <StatusDot
          connect={connect}
          synchronize={synchronize}
        />
        <span className={`${prefixCls}-title-text`}>{name}</span>
      </Fragment>;
    }
    return null;
  }, [baseInfoDs.current]);

  return (
    <div className={`${prefixCls}-re`}>
      <Modals />
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!title}
      >
        {title}
      </PrefixTitle>
      <div className={`${prefixCls}-re-card-wrap`}>
        <div className={`${prefixCls}-re-card ${prefixCls}-re-card_left`}>
          <div className={`${prefixCls}-re-card-title`}>{formatMessage({ id: `${intlPrefix}.resource.deploy` })}</div>
          <div className={`${prefixCls}-re-items`}>
            {getCounts()}
          </div>
        </div>
        <div className={`${prefixCls}-re-card ${prefixCls}-re-card_right`}>
          <div className={`${prefixCls}-re-card-title`}>{formatMessage({ id: `${intlPrefix}.instance.status` })}</div>
          <div className={`${prefixCls}-re-items`}>{getCounts('status')}</div>
        </div>
      </div>
      <div className={`${prefixCls}-re-card`}>
        <div className={`${prefixCls}-re-card-title`}>{formatMessage({ id: `${intlPrefix}.resource.dosage` })}</div>
      </div>
    </div>
  );
});

export default Content;
