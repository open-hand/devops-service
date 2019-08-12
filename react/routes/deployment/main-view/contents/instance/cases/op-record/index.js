import React, { Fragment, useState, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Popover, Icon, Button } from 'choerodon-ui';
import map from 'lodash/map';
import classnames from 'classnames';
import UserInfo from '../../../../../../../components/userInfo/UserInfo';
import { useDeploymentStore } from '../../../../../stores';
import { useInstanceStore } from '../../stores';

import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import './index.less';

const ICON_TYPE_MAPPING = {
  failed: 'cancel',
  operating: 'timelapse',
  success: 'check_circle',
};

const OpRecord = observer(({ handleClick, active }) => {
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    casesDs,
  } = useInstanceStore();
  const [cardActive, setCardActive] = useState('');
  const podKeys = useMemo(() => (['type', 'createTime', 'status', 'loginName', 'realName', 'userImage', 'podEventVO']), []);

  function getPopoverContent({ status, createTime, realName, loginName, userImage, index }) {
    return <Fragment>
      <ul className={`${prefixCls}-cases-popover-card`}>
        <li>
          <FormattedMessage id={`${intlPrefix}.instance.cases.result`} />：
          <Icon type={ICON_TYPE_MAPPING[status]} className={`${prefixCls}-cases-status-${status}`} />
          <FormattedMessage id={status} />
        </li>
        <li>
          <FormattedMessage id={`${intlPrefix}.instance.cases.time`} />：
          <span>{createTime}</span>
        </li>
        <li>
          <FormattedMessage id={`${intlPrefix}.instance.cases.operator`} />：
          <UserInfo name={realName} id={loginName} avatar={userImage} />
        </li>
      </ul>
      {index > 3 && <div className={`${prefixCls}-cases-popover-card-bottom`}>
        当前仅保留最近4条操作记录的详情
      </div>}
    </Fragment>;
  }

  const renderOperation = useMemo(() => {
    const firstRecord = casesDs.get(0);
    let realActive = cardActive || active;
    const isExist = casesDs.find(r => r.get('createTime') === realActive);

    if (!realActive || !isExist) {
      realActive = firstRecord.get('createTime');
    }

    return (
      <div className="cases-record-detail">
        {casesDs.map((record, index) => {
          const [
            type,
            createTime,
            status,
            loginName,
            realName,
            userImage,
          ] = map(podKeys, item => record.get(item));
          const cardClass = classnames({
            'operation-record-card': true,
            'operation-record-card-active': realActive === createTime,
          });
          const handleRecordClick = () => {
            setCardActive(createTime);
            handleClick(createTime);
          };
          return (
            <Popover
              content={getPopoverContent({
                createTime,
                status,
                loginName,
                realName,
                userImage,
                index,
              })}
              key={createTime}
              placement="bottom"
            >
              <div
                className={cardClass}
                onClick={index < 4 ? handleRecordClick : null}
              >
                <div className="operation-record-title">
                  <Icon type={ICON_TYPE_MAPPING[status]} className={`${prefixCls}-cases-status-${status}`} />
                  <FormattedMessage id={`${intlPrefix}.instance.cases.${type}`} />
                </div>
                <div className="operation-record-step">
                  <i className="operation-record-icon" />
                </div>
                <div className="operation-record-time">{createTime}</div>
              </div>
            </Popover>
          );
        })}
      </div>
    );
  }, [cardActive]);

  // const isDisabled = casesDs.status !== 'ready' && casesDs.length;

  return (
    <div className={`${prefixCls}-cases-record`}>
      <span className="cases-record-title">
        {formatMessage({ id: `${intlPrefix}.instance.cases.record` })}
      </span>
      <Button
        ghost
        className={`${prefixCls}-cases-record-arrow`}
        funcType="flat"
        shape="circle"
        type="primary"
        icon="navigate_before"
      />
      {renderOperation}
      <Button
        ghost
        className={`${prefixCls}-cases-record-arrow`}
        funcType="flat"
        shape="circle"
        type="primary"
        icon="navigate_next"
      />
    </div>
  );
});

export default OpRecord;
