import React, { useState, useContext } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import {
  Tooltip,
  Icon,
} from 'choerodon-ui/pro';
import { Popover } from 'choerodon-ui';
import _ from 'lodash';
import classnames from 'classnames';
import Slider from 'react-slick';
import UserInfo from '../../../../../../components/userInfo/UserInfo';
import Store from '../../../../stores';

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
  className: 'event-operation-record-detail',
};

const ICONS = {
  failed: 'cancel',
  operating: 'timelapse',
  success: 'check_circle',
};

const Record = observer(({ record, handleClick }) => {
  const {
    selectedMenu: { menuId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const [cardActive, setCardActive] = useState('');

  function handleRecordClick(createTime, podEventDTO) {
    setCardActive(createTime);
    handleClick(podEventDTO);
  }

  function operationRecord() {
    const time = record.length ? record[0].createTime : '';
    const realActive = cardActive || time;

    return (
      <Slider {...SETTING}>
        {_.map(record, ({ type, createTime, status, loginName, realName, userImage, podEventDTO }) => {
          const cardClass = classnames({
            'operation-record-card': true,
            'operation-record-card-active': realActive === createTime,
          });
          const content = (
            <ul>
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
                <FormattedMessage id={`${intlPrefix}.instance.case.operator`} />：
                <UserInfo name={realName} id={loginName} avatar={userImage} />
              </li>
            </ul>
          );
          return (
            <Popover
              content={content}
              key={createTime}
              placement="bottomRight"
              overlayClassName={`${prefixCls}-instance-cases-popover-card`}
            >
              <div
                className={cardClass}
                onClick={() => handleRecordClick(createTime, podEventDTO)}
              >
                <Icon type={ICONS[status]} className={`${prefixCls}-cases-status-${status}`} />
                <FormattedMessage id={`${intlPrefix}.instance.cases.status.${type}`} />
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
  }

  return (
    <div className={`${prefixCls}-cases-record`}>
      <span className="cases-record-title">
        {formatMessage({ id: `${intlPrefix}.instance.cases.record` })}
      </span>
      {operationRecord()}
    </div>
  );
});

export default Record;
