import React from 'react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { some, forEach } from 'lodash';
import DynamicSelect from '../../../../components/dynamic-select-new';

import './index.less';

export default injectIntl(observer(({ dataSet, tableDs, optionsDs, intlPrefix, prefixCls, modal, intl: { formatMessage } }) => {
  modal.handleOk(() => {
    forEach(dataSet.created, (createdRecord) => {
      if (createdRecord.get('projectId')) {
        tableDs.push(createdRecord);
      }
    });
  });

  function handleFilter(record) {
    const records = [...dataSet.created, ...tableDs.created];
    const flag = some(records, (creatRecord) => creatRecord.get('projectId') === record.get('id'));
    return !flag;
  }

  return (
    <div className={`${prefixCls}-project-add`}>
      <DynamicSelect
        selectDataSet={dataSet}
        optionsDataSet={optionsDs}
        optionsFilter={handleFilter} 
        selectName="projectId"
        optionKeyName="id"
        addText={formatMessage({ id: `${intlPrefix}.project.add` })}
      />
    </div>
  );
}));
