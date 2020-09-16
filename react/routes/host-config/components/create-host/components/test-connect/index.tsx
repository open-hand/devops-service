/* eslint-disable jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */
import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Icon } from 'choerodon-ui/pro';
import { Steps } from 'choerodon-ui';
import { Size } from 'choerodon-ui/lib/_util/enum';
import { useCreateHostStore } from '@/routes/host-config/components/create-host/stores';

import './index.less';

const { Step } = Steps;
const STATUS = {
  success: 'finish',
  failed: 'error',
  operating: 'process',
  wait: 'wait',
};

const TestConnect: React.FC<any> = observer(({ handleTestConnection }): any => {
  const {
    prefixCls,
    formDs,
  } = useCreateHostStore();

  const getContent = () => {
    if (formDs && formDs.current) {
      const record = formDs.current;
      const sshStatus = record.get('sshStatus') || 'wait';
      if (record.get('type') === 'distribute_test') {
        const jmeterStatus: string = record.get('jmeterStatus') || 'wait';
        return (
          <Steps progressDot size={'small' as Size}>
            <Step
              // @ts-ignore
              status={STATUS[sshStatus]}
              title="主机连接"
            />
            <Step
              // @ts-ignore
              status={STATUS[jmeterStatus]}
              title="Jmeter检测"
            />
          </Steps>
        );
      }
      let content;
      switch (sshStatus) {
        case 'success':
          content = [
            <Icon
              type="check_circle"
              className={`${prefixCls}-test-text-icon`}
            />,
            <span>成功</span>,
          ];
          break;
        case 'failed':
          content = [
            <Icon
              type="cancel"
              className={`${prefixCls}-test-text-icon`}
            />,
            <span>失败</span>,
          ];
          break;
        case 'operating':
          content = '正在进行连接测试';
          break;
        default:
          content = '未进行连接测试';
      }
      return (
        <span className={`${prefixCls}-test-text ${prefixCls}-test-text${sshStatus ? `-${sshStatus}` : ''}`}>
          {content}
        </span>
      );
    }
    return null;
  };

  return (
    <div className={`${prefixCls}-test ${prefixCls}-test${formDs && formDs.current && formDs.current.get('status') ? `-${formDs.current.get('status')}` : ''}`}>
      <Button
        onClick={handleTestConnection}
        className={`${prefixCls}-test-btn`}
      >
        测试连接
      </Button>
      {getContent()}
    </div>
  );
});

export default TestConnect;
