import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import { Select, Form } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import map from 'lodash/map';

import './index.less';

const { Option } = Select;


const LinkService = observer(({ store, projectId, envId, intlPrefix, prefixCls, formatMessage }) => {
  const {
    getServiceData,
    loadServiceData,
    setAppServiceIds,
    getAppServiceIds,
  } = store;
  
  useEffect(() => {
    loadServiceData(projectId, envId);
  }, [envId, loadServiceData, projectId, store]);

  function handleAdd() {
    setAppServiceIds([...getAppServiceIds, undefined]);
  }

  function handleDelete(index) {
    setAppServiceIds(getAppServiceIds.filter((value, key) => index !== key));
  }
  
  function handleChange(value, index) {
    const data = [...getAppServiceIds];
    data.splice(index, 1, value);
    setAppServiceIds(data);
  }

  return (
    <div className={`${prefixCls}-environment-service-modal`}>
      <Form>
        {map(getAppServiceIds, (item, index) => (<div>
          <Select
            label={formatMessage({ id: `${intlPrefix}.app-service` })}
            searchable
            required
            value={item}
            onChange={value => handleChange(value, index)}
          >
            {map(getServiceData, ({ id, name }) => (
              <Option value={id}>{name}</Option>
            ))}
          </Select>
          <Button
            shape="circle"
            icon="delete"
            onClick={() => handleDelete(index)}
          />
        </div>))}
      </Form>
      <Button
        icon="add"
        type="primary"
        onClick={handleAdd}
      >
        <FormattedMessage id={`${intlPrefix}.environment.add.service`} />
      </Button>
    </div>
  );
});

export default LinkService;
