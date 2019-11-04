import React, { Fragment, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Tooltip, Form, TextField, TextArea, Select, SelectBox, CheckBox, Radio } from 'choerodon-ui/pro';
import _ from 'lodash';

import {
  EVENT,
  TARGET_OPTIONS,
  METHOD_OPTIONS,
  TARGET_SPECIFIER,
} from '../Constants';

import './index.less';

const { Option } = Select;
const { Option: SelectBoxOption } = SelectBox;


export default observer((props) => {
  const { store, modal, notificationId, type } = props;
  const { projectId, envId, formatMessage, resourceSecurityDs, resourceSecurityCreateDs, resourceSecurityLocalStore } = store;
  const { disabledEvent, eventCheck, projectUsers, loadUsers } = resourceSecurityLocalStore;
  const [showUser, setShowUser] = useState(false);
  const [editEvents, setEditEvents] = useState([]);
  modal.handleOk(async () => {
    resourceSecurityCreateDs.current.set('envId', envId);
    resourceSecurityCreateDs.current.set('projectId', projectId);
    if (await resourceSecurityCreateDs.submit()) {
      resourceSecurityDs.query();
      return true;
    }
    return false;
  });

  useEffect(() => {
    eventCheck(projectId, envId);
  }, [projectId, envId]);

  useEffect(() => {
    if (type === 'edit') {
      resourceSecurityCreateDs.transport.read.url = `/devops/v1/projects/${projectId}/notification/${notificationId}`;
      resourceSecurityCreateDs.query().then((data) => {
        setEditEvents(data.notifyTriggerEvent);
        if (data && data.notifyObject === TARGET_SPECIFIER) {
          setShowUser(true);
        }
      });
    }
  }, [type, notificationId]);

  function selectRenderer({ value }) {
    return formatMessage({ id: `notification.event.${value}` });
  }

  function selectOptionRenderer({ value }) {
    const isDisabled = judgeDisabled(value);
    return (
      <Tooltip
        title={isDisabled ? formatMessage({ id: 'notification.event.tips' }) : ''}
      >
        {formatMessage({ id: `notification.event.${value}` })}
      </Tooltip>
    );
  }

  function selectUserRenderer({ text }) {
    return text;
  }

  function selectUserOptionRenderer({ record, text }) {
    return (<Tooltip title={record.get('loginName')}>
      {text}
    </Tooltip>);
  }
  
  function targetChange(value) {
    if (value === TARGET_SPECIFIER) {
      setShowUser(true);
      return;
    }
    setShowUser(false);
  }

  function judgeDisabled(value) {
    if (type === 'create') {
      return _.includes(disabledEvent, value);
    } else {
      return _.includes(disabledEvent, value) && !_.includes(editEvents, value);
    }
  }

  return <Fragment>
    <div className="c7ncd-resource-security">
      <Form dataSet={resourceSecurityCreateDs}>
        <Select name="notifyTriggerEvent" renderer={selectRenderer} optionRenderer={selectOptionRenderer}>
          {
          _.map(EVENT, (item) => {
            const isDisabled = judgeDisabled(item);
            return (
              <Option
                key={item}
                value={item}
                disabled={isDisabled}
              >
                {formatMessage({ id: `notification.event.${item}` })}
              </Option>
            );
          })
        }
        </Select>
        <SelectBox name="notifyType">
          {
        _.map(METHOD_OPTIONS, (item) => (
          <SelectBoxOption
            key={item}
            value={item}
          >
            <span className="select-box-option">{formatMessage({ id: `notification.method.${item}` })}</span>
          </SelectBoxOption>
        ))
        }
        </SelectBox>
        <SelectBox name="notifyObject" onChange={targetChange}>
          {_.map(TARGET_OPTIONS, (item) => (
            <SelectBoxOption
              key={item}
              value={item}
            >
              <span className="select-box-option">{formatMessage({ id: `notification.target.${item}` })}</span>
            </SelectBoxOption>
          ))}
        </SelectBox>
        {
          !showUser ? null
            : <Select name="userRelIds" searchable optionRenderer={selectUserOptionRenderer} renderer={selectUserRenderer} />
        }
      </Form>
    </div>
  </Fragment>;
});
