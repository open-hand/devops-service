import React, { Fragment, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import { Select, Form, SelectBox } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import map from 'lodash/map';

import './index.less';

const { Option } = Select;


const Permission = observer(({ store, projectId, envId, intlPrefix, prefixCls, formatMessage }) => {
  const {
    getUsersData,
    loadUsers,
    setUserIds,
    getUserIds,
    getSkipCheckPermission,
    setSkipCheckPermission,
  } = store;
  
  useEffect(() => {
    loadUsers(projectId, envId);
  }, [envId, loadUsers, projectId, store]);

  function handleBoxChange(value) {
    setSkipCheckPermission(value);
  }

  function handleAdd() {
    setUserIds([...getUserIds, undefined]);
  }

  function handleDelete(index) {
    setUserIds(getUserIds.filter((value, key) => index !== key));
  }
  
  function handleChange(value, index) {
    const data = [...getUserIds];
    data.splice(index, 1, value);
    setUserIds(data);
  }

  return (<Fragment>
    <Form className={`${prefixCls}-environment-permission-modal`}>
      <SelectBox
        value={getSkipCheckPermission}
        label={formatMessage({ id: `${intlPrefix}.set-operator` })}
        onChange={handleBoxChange}
      >
        <Option value>
          {formatMessage({ id: `${intlPrefix}.member.all` })}
        </Option>
        <Option value={false}>
          {formatMessage({ id: `${intlPrefix}.member.specific` })}
        </Option>
      </SelectBox>
      {!getSkipCheckPermission && (<div>
        {map(getUserIds, (item, index) => (<div>
          <Select
            label={formatMessage({ id: `${intlPrefix}.project.member` })}
            searchable
            required
            value={item}
            onChange={value => handleChange(value, index)}
            className="member-select-item"
          >
            {map(getUsersData, ({ iamUserId, realName, loginName }) => (
              <Option value={iamUserId}>{realName}&nbsp;{loginName}</Option>
            ))}
          </Select>
          <Button
            shape="circle"
            icon="delete"
            onClick={() => handleDelete(index)}
          />
        </div>))}
        <Button
          icon="add"
          type="primary"
          onClick={handleAdd}
        >
          <FormattedMessage id={`${intlPrefix}.add.member`} />
        </Button>
      </div>)}
    </Form>
  </Fragment>);
});

export default Permission;
