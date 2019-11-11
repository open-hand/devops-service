import React, { useMemo, useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Choerodon } from '@choerodon/boot';
import { Form, Tooltip } from 'choerodon-ui/pro';
import { some, map, findIndex } from 'lodash';
import DynamicSelect from '../../../../../../../components/dynamic-select-new';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const LinkService = observer((props) => {
  const { modal, store, tree, onOk, intlPrefix, intl: { formatMessage }, modalStores } = props;
  const { linkServiceDs, linkServiceOptionsDs } = modalStores;


  useEffect(() => {
    linkServiceDs.getField('appServiceId').set('options', linkServiceOptionsDs);
    linkServiceOptionsDs.query();
  }, []);

  modal.handleOk(async () => {
    const servers = map(linkServiceDs.created, ({ data: { appServiceId } }) => appServiceId);
    if (findIndex(servers, (item) => !item) > -1) return false;
    if (!servers || servers.length === 0) return false;

    try {
      const res = await onOk(servers);
      if (!handlePromptError(res)) return false;

      tree.query();
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });


  function optionsFilter(optionRecord) {
    const flag = some(linkServiceDs.created, (item) => item.get('appServiceId') === optionRecord.get('id'));
    return !flag;
  }
  
  function optionsRenderer({ record }) {
    const name = record.get('name');
    return <Tooltip title={name}>{name}</Tooltip>;
  }

  return (
    <Fragment>
      <DynamicSelect
        selectDataSet={linkServiceDs} 
        optionsFilter={optionsFilter} 
        optionsRenderer={optionsRenderer}
        selectName="appServiceId"
        addText={formatMessage({ id: `${intlPrefix}.environment.add.service` })}
      />
    </Fragment>
  );
});

export default injectIntl(LinkService);
