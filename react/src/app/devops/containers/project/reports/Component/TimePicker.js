import React, { Component } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Button, DatePicker } from 'choerodon-ui';
import moment from 'moment';
import './TimePicker.scss';

const { RangePicker } = DatePicker;
const ButtonGroup = Button.Group;

function TimePicker(props) {
  const { startTime, endTime, store, func, type, onChange, unlimit } = props;
  const handleClick = (val) => {
    store.setEndTime(moment());
    switch (val) {
      case 'today':
        store.setStartTime(moment());
        onChange && onChange('today');
        func();
        break;
      case 'seven':
        store.setStartTime(moment().subtract(6, 'days'));
        onChange && onChange('seven');
        func();
        break;
      case 'thirty':
        store.setStartTime(moment().subtract(29, 'days'));
        onChange && onChange('thirty');
        func();
        break;
      default:
        store.setStartTime(moment().subtract(6, 'days'));
        func();
        break;
    }
  };

  const disabledDate = current => current && current > moment().endOf('day');

  return (
    <div className="c7n-report-date-wrap">
      <div className="c7n-report-time-btn">
        <ButtonGroup>
          <Button
            style={{ backgroundColor: type === 'today' ? 'rgba(0,0,0,.08)' : '' }}
            funcType="flat"
            onClick={handleClick.bind(this, 'today')}
          >
            <FormattedMessage id="report.data.today" />
          </Button>
          <Button
            style={{ backgroundColor: type === 'seven' ? 'rgba(0,0,0,.08)' : '' }}
            funcType="flat"
            onClick={handleClick.bind(this, 'seven')}
          >
            <FormattedMessage id="report.data.seven" />
          </Button>
          <Button
            style={{ backgroundColor: type === 'thirty' ? 'rgba(0,0,0,.08)' : '' }}
            funcType="flat"
            onClick={handleClick.bind(this, 'thirty')}
          >
            <FormattedMessage id="report.data.thirty" />
          </Button>
        </ButtonGroup>
      </div>
      <div
        className="c7n-report-time-pick"
        style={{ backgroundColor: type === '' ? 'rgba(0,0,0,.08)' : '' }}
      >
        <RangePicker
          disabledDate={disabledDate}
          value={[startTime, endTime]}
          allowClear={false}
          onChange={(date, dateString) => {
            if (moment(dateString[1]).format() > moment(dateString[0]).add(29, 'days').format() && !unlimit) {
              Choerodon.prompt('报表暂支持最多查看30天，已自动截取开始日期后30天。');
              store.setStartTime(moment(dateString[0]));
              store.setEndTime(moment(dateString[0]).add(29, 'days'));
              store.setStartDate(moment(dateString[0]));
              store.setEndDate(moment(dateString[0]).add(29, 'days'));
            } else {
              store.setStartTime(moment(dateString[0]));
              store.setEndTime(moment(dateString[1]));
              store.setStartDate(moment(dateString[0]));
              store.setEndDate(moment(dateString[1]));
            }
            onChange && onChange('');
            func();
          }}
        />
      </div>
    </div>
  );
}

export default withRouter(injectIntl(TimePicker));
