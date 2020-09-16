import React from 'react';

import { Action } from '@choerodon/boot';
import UserInfo from '@/components/userInfo';
import TimePopover from '@/components/timePopover';
import StatusTagOutLine from '../../components/statusTagOutLine';
import eventStopProp from '../../../../../../utils/eventStopProp';
import { useHostConfigStore } from '../../../../stores';

interface HostsItemProps {
}

const HostsItem:React.FC<HostsItemProps> = () => {
  const {
    prefixCls,
    formatMessage,
  } = useHostConfigStore();

  function handleCheck() {}

  function handleDelete() {}

  function handleModify() {}

  const actionData = [
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
  ];

  return (
    <div className={`${prefixCls}-content-list-item`}>
      <div className={`${prefixCls}-content-list-item-header`}>
        <div className={`${prefixCls}-content-list-item-header-left`}>
          <div className={`${prefixCls}-content-list-item-header-left-top`}>
            <StatusTagOutLine status="success" />
            <span className={`${prefixCls}-content-list-item-header-left-top-name`}>
              主机001
            </span>
          </div>
          <div className={`${prefixCls}-content-list-item-header-left-bottom`}>
            <div>
              <UserInfo
                name="翁恺敏"
                showName={false}
                avatar="https://minio.choerodon.com.cn/iam-service/file_11b8ef213e724602abd9facf66c0271a_u%3D2233506214%2C1519914781"
              />
            </div>
            <div>
              <span>更新</span>
              <TimePopover
                style={{
                  color: 'rgba(58, 52, 95, 1)',
                  marginLeft: '4px',
                }}
                content="2020-09-04 14:27:23"
              />
            </div>
          </div>
        </div>
        <div className={`${prefixCls}-content-list-item-header-right`}>
          <Action
            data={actionData}
            onClick={eventStopProp}
          />
        </div>
      </div>
      <main className={`${prefixCls}-content-list-item-main`}>
        <div className={`${prefixCls}-content-list-item-main-item`}>
          <span>
            IP与端口
          </span>
          <span>
            172.23.40.37:22
          </span>
        </div>
        <div className={`${prefixCls}-content-list-item-main-item`}>
          <span>
            Jmeter端口
          </span>
          <span>
            8080
          </span>
        </div>
        <div className={`${prefixCls}-content-list-item-main-item`}>
          <span>
            Jmeter路径
          </span>
          <span>
            /home/apache-jmeter -
            4.0：8080
          </span>
        </div>
      </main>
    </div>
  );
};

export default HostsItem;
