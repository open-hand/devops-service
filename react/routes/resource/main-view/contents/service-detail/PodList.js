import React, { Fragment, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { Button, Icon, Popover } from 'choerodon-ui';
import ReactEcharts from 'echarts-for-react';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import TimePopover from '../../../../../components/timePopover';

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

  function renderRegistry(containers) {
    const list = map(containers, ({ name, ready, registry }) => (
      <div key={name}>
        <div>
          <span>{name}</span>
          <Button
            icon="find_in_page"
            shape="circle"
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
      <div>
        {list[0]}
        {list.length > 1 && (
          <Popover
            content={list}
          >
            <Icon type="expand_more" />
          </Popover>
        )}
      </div>
    );
  }
  
  function getOption(data, timeList, name) {
    return ({
      xAxis: {
        type: 'category',
        boundaryGap: false,
        show: false,
        data: timeList,
      },
      yAxis: {
        type: 'value',
        name,
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        axisLabel: {
          show: false,
        },
        splitLine: {
          show: false,
        },
      },
      series: [{
        data,
        type: 'line',
        areaStyle: {},
        itemStyle: {
          opacity: 0,
        },
      }],
    });
  }

  return (
    <Fragment>
      <div className={`${prefixCls}-detail-content-section-title`}>
        <FormattedMessage id={`${intlPrefix}.pods`} />
      </div>
      <div className="detail-content-section-detail">
        {map(record.get('podLiveInfos'), ({
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
        }) => (
          <ul className="service-detail-pod-list" key={podId}>
            <li className="service-detail-pod-item">
              <span className="service-detail-pod-item-name">{podName}</span>
              <span className="service-detail-pod-item-key">
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
              {renderRegistry(containers)}
            </li>
            <li className="service-detail-pod-echarts">
              <ReactEcharts
                option={getOption(cpuUsedList, timeList, 'CPU 1500m')}
                style={{ height: '0.42rem' }}
              />
              <ReactEcharts
                option={getOption(memoryUsedList, timeList, 'Memory 1600MiB')}
                style={{ height: '0.42rem' }}
              />
            </li>
          </ul>
        ))}
      </div>
    </Fragment>
  );
});

export default PodList;
