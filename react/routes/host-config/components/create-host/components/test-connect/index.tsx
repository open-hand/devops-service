/* eslint-disable jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */
import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import {Button, Icon, Tooltip} from 'choerodon-ui/pro';
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
      const hostStatus = record.get('hostStatus') || 'wait';
      if (record.get('type') === 'distribute_test') {
        const jmeterStatus: string = record.get('jmeterStatus') || 'wait';
        return (
          <Steps progressDot size={'small' as Size}>
            <Step
              // @ts-ignore
              status={STATUS[hostStatus]}
              title={hostStatus === 'failed' && record.get('hostCheckError') ? (
                <span>
                  主机连接
                  <Tooltip title={record.get('hostCheckError')}>
                    <Icon type="cancel" className={`${prefixCls}-test-failed-icon`} />
                  </Tooltip>
                </span>
              ) : '主机连接'}
            />
            <Step
              // @ts-ignore
              status={STATUS[jmeterStatus]}
              title={jmeterStatus === 'failed' && record.get('jmeterCheckError') ? (
                <span>
                  Jmeter检测
                  <Tooltip title={record.get('jmeterCheckError')}>
                    <Icon type="cancel" />
                  </Tooltip>
                </span>
              ) : 'Jmeter检测'}
            />
          </Steps>
        );
      }
      let content;
      switch (hostStatus) {
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
            <Tooltip title={record.get('hostCheckError') || ''}>
              <Icon
                type="cancel"
                className={`${prefixCls}-test-text-icon`}
              />
            </Tooltip>,
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
        <span className={`${prefixCls}-test-text ${prefixCls}-test-text${hostStatus ? `-${hostStatus}` : ''}`}>
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
