import React, { useCallback } from 'react';

import { Action } from '@choerodon/boot';
import UserInfo from '@/components/userInfo';
import TimePopover from '@/components/timePopover';
import { Tooltip, Modal } from 'choerodon-ui/pro';
import StatusTagOutLine from '../../components/statusTagOutLine';
import eventStopProp from '../../../../../../utils/eventStopProp';
import { useHostConfigStore } from '../../../../stores';
import CreateHost from '../../../create-host';

const HostsItem:React.FC<any> = ({
  name,
  jmeterStatus,
  hostIp,
  sshPort,
  username,
  jmeterPort,
  jmeterPath,
  imgUrl,
  date,
  listDs,
  record,
}) => {
  const {
    prefixCls,
    formatMessage,
    intlPrefix,
  } = useHostConfigStore();

  function handleCheck() {
    record.set('jmeterStatus', 'operating');
  }

  function handleDelete() {
    listDs.delete(record, {
      key: Modal.key(),
      title: '删除主机',
      children: '确定要删除该主机配置吗？',
      okText: formatMessage({ id: 'delete' }),
      okProps: {
        color: 'red',
      },
      cancelProps: {
        color: '#000',
      },
    });
  }

  function handleModify() {
    Modal.open({
      key: Modal.key(),
      title: formatMessage({ id: `${intlPrefix}.modify` }),
      style: {
        width: 380,
      },
      drawer: true,
      children: <CreateHost />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  const getActionData = useCallback(() => (jmeterStatus !== 'operating' ? [
    {
      service: [],
      text: '校准状态',
      action: handleCheck,
    },
    {
      service: [],
      text: '修改',
      action: handleModify,
    },
    {
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: handleDelete,
    },
  ] : []), [jmeterStatus]);

  return (
    <div className={`${prefixCls}-content-list-item`}>
      <div className={`${prefixCls}-content-list-item-header`}>
        <div className={`${prefixCls}-content-list-item-header-left`}>
          <div className={`${prefixCls}-content-list-item-header-left-top`}>
            <StatusTagOutLine status={jmeterStatus} />
            <Tooltip title={name} placement="top">
              <span className={`${prefixCls}-content-list-item-header-left-top-name`}>
                {name}
              </span>
            </Tooltip>
          </div>
          <div className={`${prefixCls}-content-list-item-header-left-bottom`}>
            <div>
              <UserInfo
                name={username}
                showName={false}
                avatar={imgUrl}
              />
            </div>
            <div>
              <span>更新</span>
              <TimePopover
                style={{
                  color: 'rgba(58, 52, 95, 1)',
                  marginLeft: '4px',
                }}
                content={date}
              />
            </div>
          </div>
        </div>
        {jmeterStatus !== 'operating' && (
          <div className={`${prefixCls}-content-list-item-header-right`}>
            <Action
              data={getActionData()}
              onClick={eventStopProp}
            />
          </div>
        )}
      </div>
      <main className={`${prefixCls}-content-list-item-main`}>
        <div className={`${prefixCls}-content-list-item-main-item`}>
          <span>
            IP与端口
          </span>
          <span>
            {`${hostIp}：${sshPort}`}
          </span>
        </div>
        <div className={`${prefixCls}-content-list-item-main-item`}>
          <span>
            Jmeter端口
          </span>
          <span>
            {jmeterPort}
          </span>
        </div>
        <div className={`${prefixCls}-content-list-item-main-item`}>
          <span>
            Jmeter路径
          </span>
          <span>
            {jmeterPath}
          </span>
        </div>
      </main>
    </div>
  );
};

export default HostsItem;
