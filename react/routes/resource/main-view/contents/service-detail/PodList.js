import React, { Fragment, useMemo, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import compact from 'lodash/compact';
import isEmpty from 'lodash/isEmpty';
import { Button, Icon, Popover } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import { useNetworkDetailStore } from './stores';
import TimePopover from '../../../../../components/timePopover';
import LogSidebar from '../../../../../components/log-siderbar';

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
            {formatMessage({ id: `${intlPrefix}.registry` })}:{registry}
          </span>
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
              <span className="service-detail-pod-item-key" style={{ whiteSpace: 'nowrap' }}>
                {formatMessage({ id: `${intlPrefix}.instance.ip` })}:{podIp || '-'}
              </span>
            </div>
            <div>
              <span className="service-detail-pod-item-key" style={{ whiteSpace: 'nowrap' }}>
                {formatMessage({ id: `${intlPrefix}.node` })}: {nodeIp ? `${nodeName}(${nodeIp})` : '-'}
              </span>
            </div>
          </li>
          <li className="service-detail-pod-item">
            {renderRegistry(containers, index)}
          </li>
        </ul>
      )) : <span style={{ color: 'rgba(0,0,0,.65)' }}>{formatMessage({ id: 'nodata' })}</span>}
    </div>
    {visible && <LogSidebar visible={visible} onClose={closeLog} record={data} />}
  </Fragment>);
});

export default PodList;
