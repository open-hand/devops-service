import React, { Fragment, useMemo, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import compact from 'lodash/compact';
import isEmpty from 'lodash/isEmpty';
import { Button, Icon, Popover, Tooltip } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import ReactEcharts from 'echarts-for-react';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import TimePopover from '../../../../../components/timePopover';
import LogSidebar from '../../../../../components/log-siderbar';

const modalKey = Modal.key();
const modalStyle = {
  width: '50%',
};

const PodList = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    baseInfoDs,
    intl: { formatMessage },
  } = useNetworkDetailStore();

  const record = baseInfoDs.current;

  const [visible, setVisible] = useState(false);
  const [data, setData] = useState();

  function openLog() {
    setVisible(true);
  }
  function closeLog() {
    setVisible(false);
  }
  function handleLogClick(index, containerIndex) {
    setData({ ...record.get('podLiveInfos')[index], containerIndex });
    openLog();
  }


  function openModal(value, timeList, type) {
    const name = type === 'cpu' ? 'CPU /m' : 'Memory /MiB';
    Modal.open({
      key: modalKey,
      style: modalStyle,
      title: formatMessage({ id: `${intlPrefix}.report.${type}` }),
      children: <div>
        <ReactEcharts
          option={getOption(value, timeList, name, true)}
          style={{ height: '4rem' }}
        />
      </div>,
      okCancel: false,
      footer: null,
      maskClosable: true,
    });
  }

  function renderRegistry(containers, index) {
    const list = map(containers, ({ name, ready, registry }, containerIndex) => (
      <div key={name}>
        <div>
          <span>{name}</span>
          <Button
            icon="find_in_page"
            shape="circle"
            onClick={() => {
              handleLogClick(index, containerIndex);
            }}
          />
        </div>
        <div>
          <span className="service-detail-pod-item-key">
            {formatMessage({ id: `${intlPrefix}.registry` })}:&nbsp;
          </span>
          <span>{registry}</span>
        </div>
      </div>
    ));
    return (
      <div className="service-detail-pod-registry">
        {list[0]}
        {list.length > 1 && (
          <Popover
            content={list}
          >
            <Icon type="expand_more" className="service-detail-pod-item-icon" />
          </Popover>
        )}
      </div>
    );
  }

  function getOption(value, timeList, name, show = false) {
    const optionData = show ? {
      tooltip: {
        trigger: 'item',
        backgroundColor: '#fff',
        textStyle: {
          color: '#000',
        },
        formatter(obj) {
          return `${formatMessage({ id: `${intlPrefix}.time` })}: ${
            obj.name
          }<br/>${formatMessage({ id: `${intlPrefix}.usage` })}: ${
            obj.value
          }`;
        },
      },
    } : {
      grid: {
        bottom: 30,
      },
    };
    return ({
      ...optionData,
      color: '#7885cb',
      xAxis: {
        type: 'category',
        boundaryGap: false,
        show,
        data: timeList,
      },
      yAxis: {
        type: 'value',
        name,
        nameTextStyle: {
          fontSize: show ? 13 : 10,
        },
        axisLine: {
          show,
        },
        axisTick: {
          show,
        },
        axisLabel: {
          show,
        },
        splitLine: {
          show: false,
        },
      },
      series: [{
        data: value,
        type: 'line',
        areaStyle: {},
        itemStyle: {
          opacity: 0,
        },
      }],
    });
  }

  return (<Fragment>
    <div className={`${prefixCls}-detail-content-section-title`}>
      <FormattedMessage id={`${intlPrefix}.pods`} />
    </div>
    <div className="detail-content-section-detail">
      {!isEmpty(record.get('podLiveInfos')) ? map(compact(record.get('podLiveInfos')), ({
        containers,
        cpuUsedList,
        memoryUsedList,
        timeList,
        nodeIp,
        nodeName,
        podName,
        podId,
        podIp,
        creationDate,
      }, index) => (
        <ul className="service-detail-pod-list" key={podId}>
          <li className="service-detail-pod-item">
            <span className="service-detail-pod-item-name">{podName}</span>
            <span className="service-detail-pod-item-time">
              <TimePopover content={creationDate} />
            </span>
          </li>
          <li className="service-detail-pod-item">
            <div>
              <span className="service-detail-pod-item-key">
                {formatMessage({ id: `${intlPrefix}.instance.ip` })}:&nbsp;
              </span>
              <span>{podIp || '-'}</span>
            </div>
            <div>
              <span className="service-detail-pod-item-key">
                {formatMessage({ id: `${intlPrefix}.node` })}:&nbsp;
              </span>
              <span>{nodeIp ? `${nodeName}(${nodeIp})` : '-'}</span>
            </div>
          </li>
          <li className="service-detail-pod-item">
            {renderRegistry(containers, index)}
          </li>
          {timeList && (
            <Fragment>
              <li className="service-detail-pod-echarts">
                <Tooltip title={formatMessage({ id: `${intlPrefix}.report.cpu.click` })}>
                  <div onClick={() => openModal(cpuUsedList, timeList, 'cpu')}>
                    <ReactEcharts
                      option={getOption(cpuUsedList, timeList, 'CPU /m')}
                      style={{ height: '0.42rem', width: '1.2rem' }}
                    />
                  </div>
                </Tooltip>
                <Tooltip title={formatMessage({ id: `${intlPrefix}.report.memory.click` })}>
                  <div onClick={() => openModal(memoryUsedList, timeList, 'memory')}>
                    <ReactEcharts
                      option={getOption(memoryUsedList, timeList, 'Memory /MiB')}
                      style={{ height: '0.42rem', width: '1.2rem' }}
                    />
                  </div>
                </Tooltip>
              </li>
            </Fragment>
          )}
        </ul>
      )) : <FormattedMessage id="nodata" />}
    </div>
    {visible && <LogSidebar visible={visible} onClose={closeLog} record={data} />}
  </Fragment>);
});

export default PodList;
