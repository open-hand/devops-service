import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import EnvDetail from '../../../../../../components/env-detail';
import { useResourceStore } from '../../../../stores';
import { useREStore } from '../stores';

const REModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    treeDs,
  } = useResourceStore();
  const {
    baseInfoDs,
    resourceCountDs,
  } = useREStore();

  function refresh() {
    baseInfoDs.query();
    resourceCountDs.query();
    treeDs.query();
  }

  function openEnvDetail() {
    Modal.open({
      key: Modal.key(),
      title: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      children: <EnvDetail record={baseInfoDs.current} />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      icon: 'find_in_page',
      handler: openEnvDetail,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 1,
    }];
  }

  return <HeaderButtons items={getButtons()} />;
});

export default REModals;
