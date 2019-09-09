import React, { useState, useMemo, useRef, useCallback, useEffect } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon, Tooltip } from 'choerodon-ui';
import map from 'lodash/map';
import classnames from 'classnames';
import UserInfo from '../../../../../../../components/userInfo/UserInfo';
import { useResourceStore } from '../../../../../stores';
import { useInstanceStore } from '../../stores';

import './index.less';

const ICON_TYPE_MAPPING = {
  failed: 'cancel',
  operating: 'timelapse',
  success: 'check_circle',
};

const OpCard = ({ record, isActive, disabled, intlPrefix, prefixCls, formatMessage, onClick }) => {
  const podKeys = useMemo(() => (['type', 'createTime', 'status', 'loginName', 'realName', 'userImage', 'podEventVO']), []);
  const [
    type,
    createTime,
    status,
    loginName,
    realName,
    userImage,
  ] = map(podKeys, (item) => record.get(item));
  const cardClass = classnames({
    'operation-record-card': true,
    'operation-record-card-active': isActive,
  });
  const handleClick = useCallback(() => {
    !disabled && onClick(createTime);
  }, [createTime, disabled]);

  return (
    <div
      className={cardClass}
      onClick={handleClick}
    >
      <div className="operation-record-title">
        <Tooltip title={formatMessage({ id: status })}>
          <Icon type={ICON_TYPE_MAPPING[status]} className={`${prefixCls}-cases-status-${status}`} />
        </Tooltip>
        <FormattedMessage id={`${intlPrefix}.instance.cases.${type}`} />
      </div>
      <div className="operation-record-step">
        <i className="operation-record-icon" />
      </div>
      <div className="operation-record-user"><UserInfo name={realName} id={loginName} avatar={userImage} /></div>
      <div className="operation-record-time">{createTime}</div>
    </div>
  );
};

const OpRecord = observer(({ handleClick, active }) => {
  const rowRef = useRef(null);
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id, parentId } },
  } = useResourceStore();
  const {
    intl: { formatMessage },
    casesDs,
  } = useInstanceStore();
  const [cardActive, setCardActive] = useState('');

  useEffect(() => {
    setCardActive('');
  }, [id, parentId]);

  function handleRecordClick(time) {
    setCardActive(time);
    handleClick(time);
  }

  function renderOperation() {
    let realActive = cardActive || active;
    const isExist = casesDs.find((r) => r.get('createTime') === realActive);

    if (!realActive || !isExist) {
      const firstRecord = casesDs.get(0);
      realActive = firstRecord.get('createTime');
    }

    return (
      <div ref={rowRef} className="cases-record-detail">
        {casesDs.map((record, index) => {
          const createTime = record.get('createTime');
          return <OpCard
            key={createTime}
            disabled={index >= 4}
            isActive={realActive === createTime}
            formatMessage={formatMessage}
            record={record}
            prefixCls={prefixCls}
            intlPrefix={intlPrefix}
            onClick={handleRecordClick}
          />;
        })}
      </div>
    );
  }

  return (
    <div className={`${prefixCls}-cases-record`}>
      <span className="cases-record-title">
        {formatMessage({ id: `${intlPrefix}.instance.cases.record` })}
      </span>
      {renderOperation()}
    </div>
  );
});

export default OpRecord;
