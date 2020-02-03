import React from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import some from 'lodash/some';
import DynamicSelect from '../../../../components/dynamic-select-new';

import './index.less';

export default injectIntl(observer(({ dataSet, optionsDs, intlPrefix, prefixCls, modal, detailDs, intl: { formatMessage } }) => {
  modal.handleOk(async () => {
    try {
      if (await dataSet.submit() !== false) {
        detailDs.query();
        dataSet.query();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function handleFilter(record) {
    const flag = some(dataSet.created, (creatRecord) => creatRecord.get('project') === record.get('id'));
    return !flag;
  }

  return (
    <div className={`${prefixCls}-project-add`}>
      <DynamicSelect
        selectDataSet={dataSet}
        optionsDataSet={optionsDs}
        optionsFilter={handleFilter} 
        selectName="project"
        optionKeyName="id"
        addText={formatMessage({ id: `${intlPrefix}.project.add` })}
      />
    </div>
  );
}));
