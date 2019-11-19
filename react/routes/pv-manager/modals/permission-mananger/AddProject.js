import React, { Fragment, useEffect } from 'react';
import { Form, Select, Button } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import some from 'lodash/some';

import './index.less';

export default injectIntl(observer(({ dataSet, optionsDs, intlPrefix, prefixCls, modal }) => {
  useEffect(() => {
    optionsDs.query();
    handleCreate();
  }, []);

  modal.handleOk(async () => {
    try {
      if (await dataSet.submit() !== false) {
        dataSet.query();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function handleDelete(record) {
    dataSet.remove(record);
  }

  function handleCreate() {
    dataSet.create();
  }

  function handleFilter(record) {
    const flag = some(dataSet.created, (creatRecord) => creatRecord.get('projectId') === record.get('id'));
    return !flag;
  }

  return (
    <div className={`${prefixCls}-project-add`}>
      {map(dataSet.created, (record) => (
        <div className={`${prefixCls}-project-add-item`}>
          <Form record={record}>
            <Select name="projectId" searchable optionsFilter={handleFilter} />
          </Form>
          <Button
            icon="delete"
            shape="circle"
            onClick={() => handleDelete(record)}
            disabled={dataSet.created.length === 1}
            className={`${prefixCls}-project-add-button`}
          />
        </div>
      ))}
      <Button
        color="primary"
        icon="add"
        onClick={handleCreate}
      >
        <FormattedMessage id={`${intlPrefix}.project.add`} />
      </Button>
    </div>
  );
}));
