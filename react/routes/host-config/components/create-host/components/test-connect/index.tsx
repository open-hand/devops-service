/* eslint-disable jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */
import React, { useMemo, useState } from 'react';
import { useCreateHostStore } from '@/routes/host-config/components/create-host/stores';

import './index.less';

const TestConnect: React.FC<any> = (): any => {
  const {
    prefixCls,
    formDs,
  } = useCreateHostStore();

  const handleClick = () => {

  };

  const getContent = () => {
  };

  return (
    <div className={`${prefixCls}-test`}>
      <div
        onClick={handleClick}
        className={`${prefixCls}-test-btn`}
      >
        测试连接
      </div>
      {getContent()}
    </div>
  );
};

export default TestConnect;
