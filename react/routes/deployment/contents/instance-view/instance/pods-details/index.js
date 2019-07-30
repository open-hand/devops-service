import React, { useContext, useMemo, memo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Table, DataSet } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import TableDataSet from './stores';
import Store from '../../../../stores';

import './index.less';

const PodDetail = memo(() => {
  const {
    prefixCls,
    intlPrefix,
    intl,
    AppState: {
      currentMenuType: {
        id,
      },
    },
    selectedMenu: { menuId, parentId },
  } = useContext(Store);
  const tableDs = useMemo(() => {
    const [envId, appId] = parentId.split('-');

    return new DataSet(TableDataSet({
      intl,
      intlPrefix,
      projectId: id,
      envId,
      appId,
      istId: menuId,
    }));
  }, [id, intl, intlPrefix, menuId, parentId]);

  const columns = useMemo(() => ([
    {
      name: 'pod',
    },
  ]), []);

  return (
    <Table
      dataSet={tableDs}
      border={false}
      queryBar="none"
      columns={columns}
    />
  );
});

export default PodDetail;
