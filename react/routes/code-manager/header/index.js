import React from 'react';
import { observer } from 'mobx-react-lite';
import { Breadcrumb } from '@choerodon/boot';
import { useCodeManagerStore } from '../stores';
import './index.less';

const CodeManagerHeader = observer(() => {
  const {
    prefixCls,
    codeManagerStore: {
      getSelectedMenu: { menuType },
      getNoHeader,
    },
  } = useCodeManagerStore();
  

  return <div className="c7ncd-code-manager">
    {!getNoHeader && <div className={`${prefixCls}-header-placeholder`} />}
    <Breadcrumb className={`${prefixCls}-header-no-bottom`} title="代码管理" />
  </div>;
});

export default CodeManagerHeader;
