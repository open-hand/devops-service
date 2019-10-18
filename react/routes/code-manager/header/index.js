import React from 'react';
import { observer } from 'mobx-react-lite';
import { Breadcrumb } from '@choerodon/boot';
import { useCodeManagerStore } from '../stores';
import './index.less';

const CodeManagerHeader = observer(() => {
  const {
    prefixCls,
    codeManagerStore: { getNoHeader },
  } = useCodeManagerStore();

  return <div className="c7ncd-code-manager">
    {!getNoHeader && <div className={`${prefixCls}-header-placeholder`} />}
    <Breadcrumb className={`${prefixCls}-header-no-bottom`} />
  </div>;
});

export default CodeManagerHeader;
