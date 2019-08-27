import React from 'react';
import { Progress } from 'choerodon-ui';

import './index.less';

export const TitleWrap = ({ children }) => (<div className="c7ncd-page-title">
  {children}
</div>);

export const FailBack = () => <TitleWrap>
  <Progress type="loading" size="small" />
</TitleWrap>;

export default function PrefixTitle({ children }) {
  return children
    ? <FailBack />
    : <TitleWrap>
      {children}
    </TitleWrap>;
}
