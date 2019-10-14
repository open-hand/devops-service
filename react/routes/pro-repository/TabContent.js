import React from 'react';
import { PageTab, PageWrap } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import Content from './Content';
import { useRepositoryStore } from './stores';

export default observer((props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
  } = useRepositoryStore();

  return (
    <PageWrap noHeader={[]}>
      <PageTab title={formatMessage({ id: `${intlPrefix}.tab.info.project` })} tabKey="choerodon.code.project.general-info" component={Content} />
      <PageTab title={formatMessage({ id: `${intlPrefix}.tab.repo` })} tabKey="choerodon.code.project.general-repository" component={Content} />
    </PageWrap>
  );
});
