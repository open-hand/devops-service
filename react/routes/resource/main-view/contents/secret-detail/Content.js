import React from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores/index';
import { useSecretDetailStore } from './stores'; 
import Modals from './modals';

import './index.less';

const Content = observer(() => {
  const resourceStore = useResourceStore();
  const {
    prefixCls,
    intlPrefix,
  } = resourceStore;
  const { detailDs } = useSecretDetailStore();

  const {
    childrenStore,
    testStore,
  } = useMainStore();

  testStore.store = detailDs;
    
  childrenStore.setDetailDs(detailDs);
  const record = detailDs.current;
  if (!record) return <span>loading</span>;

  return (
    <div className={`${prefixCls}-secret-detail`}>
      <Modals />
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="vpn_key" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record.get('name')}</span>
      </div>
      <div className={`${prefixCls}-detail-content-section-title`}>
        <FormattedMessage id={`${intlPrefix}.key.value`} />
      </div>
      {map(record.get('value'), (value, key) => (
        <div className="secret-detail-section">
          <div className="secret-detail-section-title">
            <span>{key}</span>
          </div>
          <div className="secret-detail-section-content">
            <span>{value}</span>
          </div>
        </div>
      ))}
    </div>
  );
});

export default Content;
