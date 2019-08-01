import React, { useState, useContext, useMemo, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import {
  Tooltip,
  Icon,
} from 'choerodon-ui/pro';
import { Popover } from 'choerodon-ui';
import map from 'lodash/map';
import classnames from 'classnames';
import Slider from 'react-slick';
import UserInfo from '../../../../../../components/userInfo/UserInfo';
import { useCasesStore } from './stores';
import { useDeploymentStore } from '../../../../stores';

import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import './style/Record.less';

const SETTING = {
  dots: false,
  infinite: false,
  arrows: true,
  draggable: false,
  speed: 500,
  slidesToShow: 5,
  slidesToScroll: 1,
  prevArrow: <div><Icon type="navigate_before" className="operation-slick-arrow" /></div>,
  nextArrow: <div><Icon type="navigate_next" className="operation-slick-arrow" /></div>,
  className: 'cases-record-detail',
};

const ICONS = {
  failed: 'cancel',
  operating: 'timelapse',
  success: 'check_circle',
};

const Operation = observer(({ handleClick }) => {
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    casesDs,
  } = useCasesStore();
  const [cardActive, setCardActive] = useState('');

  const handleRecordClick = useCallback((createTime, podEventVO) => {
    setCardActive(createTime);
    handleClick(podEventVO);
  }, [handleClick]);

  const renderOperation = useMemo(() => {
    const firstRecord = casesDs.get(0);
    const time = firstRecord.get('createTime');
    const realActive = cardActive || time;

    return (
      <Slider {...SETTING}>
        {casesDs.map((record) => {
          const [type, createTime, status, loginName, realName, userImage, podEventVO] = map(['type', 'createTime', 'status', 'loginName', 'realName', 'userImage', 'podEventVO'], item => record.get(item));
          const cardClass = classnames({
            'operation-record-card': true,
            'operation-record-card-active': realActive === createTime,
          });
          const content = (
            <ul className={`${prefixCls}-cases-popover-card`}>
              <li>
                <FormattedMessage id={`${intlPrefix}.instance.cases.result`} />：
                <Icon type={ICONS[status]} className={`${prefixCls}-cases-status-${status}`} />
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
          );
          return (
            <Popover
              content={content}
              key={createTime}
              placement="bottomRight"
            >
              <div
                className={cardClass}
                onClick={() => handleRecordClick(createTime, podEventVO)}
              >
                <div className="operation-record-title">
                  <Icon type={ICONS[status]} className={`${prefixCls}-cases-status-${status}`} />
                  <FormattedMessage id={`${intlPrefix}.instance.cases.${type}`} />
                </div>
                <div className="operation-record-step">
                  <Icon type="wait_circle" className="operation-record-icon" />
                </div>
                <div className="operation-record-line" />
                <div className="operation-record-time">{createTime}</div>
              </div>
            </Popover>
          );
        })}
      </Slider>
    );
  }, [cardActive, casesDs, handleRecordClick, intlPrefix, prefixCls]);

  return (
    <div className={`${prefixCls}-cases-record`}>
      <span className="cases-record-title">
        {formatMessage({ id: `${intlPrefix}.instance.cases.record` })}
      </span>
      {renderOperation}
    </div>
  );
});

export default Operation;
