import React, { useCallback, useMemo } from 'react';

import { Action } from '@choerodon/boot';
import UserInfo from '@/components/userInfo';
import TimePopover from '@/components/timePopover';
import { Tooltip, Modal } from 'choerodon-ui/pro';
import StatusTagOutLine from '../../components/statusTagOutLine';
import eventStopProp from '../../../../../../utils/eventStopProp';
import { useHostConfigStore } from '../../../../stores';
import CreateHost from '../../../create-host';

const HostsItem:React.FC<any> = ({
  sshPort, // 主机ssh的端口
  hostStatus, // 主机状态
  hostIp, // 主机ip
  name, // 主机名称
  id, // 主键
  jmeterPort, // jmeter进程的端口号
  jmeterStatus, // jmeter状态
  authType, // 认证类型
  type, // 主机类型 deploy / distribute_test
  jmeterPath, // jmeter二进制文件的路径
  username, // 用户名
  listDs,
  record,
}) => {
  const {
    prefixCls,
    formatMessage,
    intlPrefix,
    refresh,
  } = useHostConfigStore();

  const getMainStatus = useMemo(() => {
    if (jmeterStatus === 'success' && hostStatus === 'success') {
      return 'success';
    }
    if (jmeterStatus === 'failed' || hostStatus === 'failed') {
      return 'failed';
    }
    return 'operating';
  }, [jmeterStatus, hostStatus]);

  function handleCheck() {
    record.set('hostStatus', 'operating');
  }

  async function handleDelete() {
    const modalProps = {
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
    };
    const res = await listDs.delete(record, modalProps);
    if (res && res.success) {
      refresh();
    }
  }

  function handleModify() {
    Modal.open({
      key: Modal.key(),
      title: formatMessage({ id: `${intlPrefix}.modify` }),
      style: {
        width: 380,
      },
      drawer: true,
      children: <CreateHost hostId={id} refresh={refresh} />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  const getActionData = useCallback(() => (getMainStatus !== 'operating' ? [
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
  ] : []), [getMainStatus]);

  return (
    <div className={`${prefixCls}-content-list-item`}>
      <div className={`${prefixCls}-content-list-item-header`}>
        <div className={`${prefixCls}-content-list-item-header-left`}>
          <div className={`${prefixCls}-content-list-item-header-left-top`}>
            <StatusTagOutLine status={getMainStatus} />
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
        {getMainStatus !== 'operating' && (
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
            {hostIp && sshPort ? `${hostIp}：${sshPort}` : '-'}
          </span>
        </div>
        {
          type === 'distribute_test' && (
            <>
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
            </>
          )
        }
      </main>
    </div>
  );
};

export default HostsItem;
