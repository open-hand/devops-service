import React, { Fragment } from 'react';
import EmptyPage from '../../../../../components/empty-page';
import HeaderButtons from '../../../../../components/header-buttons';
import { useResourceStore } from '../../../stores';

export default function EmptyShown() {
  const {
    intl: { formatMessage },
    treeDs,
  } = useResourceStore();

  function refresh() {
    treeDs.query();
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 1,
    }];
  }

  return <Fragment>
    <HeaderButtons items={getButtons()} />
    <EmptyPage
      title="暂无环境"
      describe="当前项目下无环境，请创建"
    />
  </Fragment>;
}
