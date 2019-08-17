import React, { Fragment, useCallback, useMemo } from 'react';
import PropTypes from 'prop-types';
import { Form, Select, Button } from 'choerodon-ui';
import map from 'lodash/map';
import findKey from 'lodash/findKey';
import omit from 'lodash/omit';

import './index.less';

const { Item: FormItem } = Form;
const formItemLayout = {
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 24 },
  },
};

// eslint-disable-next-line func-names
const selectKeyGen = (function* (id) {
  while (true) {
    // eslint-disable-next-line no-plusplus
    yield `key${id++}`;
  }
}(1));

function getSelectKey() {
  return selectKeyGen.next().value;
}

function getRepeatKey(data, value, field) {
  return findKey(omit(data, ['keys', field]), (item) => item === value);
}

export function SimpleSelect({ uid, label, options, onDelete, removeable, form }) {
  const handleDelete = useCallback(() => {
    onDelete(uid);
  }, [uid]);

  function uniqValid(rule, value, callback) {
    const { field } = rule;
    const data = form.getFieldsValue();
    const repeatKey = getRepeatKey(data, value, field);

    if (repeatKey) {
      callback('不可重复选择');
    }

    callback();
  }

  return <FormItem
    {...formItemLayout}
    className="c7ncd-dynamic-select"
    key={uid}
  >
    {form.getFieldDecorator(uid, {
      validateTrigger: ['onChange'],
      rules: [
        {
          required: true,
          validator: uniqValid,
        },
      ],
    })(<Select
      required
      searchable
      label={label}
    >
      {options}
    </Select>)}
    <Button
      className="c7ncd-dynamic-select-remove"
      disabled={!removeable}
      shape="circle"
      icon="delete"
      onClick={handleDelete}
    />
  </FormItem>;
}

SimpleSelect.propTypes = {
  uid: PropTypes.string.isRequired,
  options: PropTypes.array,
  label: PropTypes.string,
  removeable: PropTypes.bool,
  onDelete: PropTypes.func,
};

SimpleSelect.defaultProps = {
  label: '',
  options: [],
};

export default function DynamicSelect({ form, label, fieldKeys, optionData, addText }) {
  function add() {
    const keys = form.getFieldValue('keys');
    if (!keys) return;
    const newKey = getSelectKey();
    const nextKeys = keys.concat(newKey);
    form.setFieldsValue({ keys: nextKeys });
  }

  function remove(k) {
    const keys = form.getFieldValue('keys');
    if (keys.length === 1) {
      return;
    }
    form.setFieldsValue({
      keys: keys.filter((key) => key !== k),

      /**
       * NOTE: 移除keys中的表单标识，会先触发下面的data更新，而此时在获取的表单信息中，被移除的项依然存在
       * 导致先渲染options，再更新dom，结果是每次可选择的项目中没有刚被删除的项目
       * */

      [k]: undefined,
    });
  }

  const options = useMemo(() => map(optionData, ({ id, name }) => {
    const selectedValues = Object.values(omit(fieldKeys, 'keys'));
    return <Select.Option
      disabled={selectedValues.includes(id)}
      key={id}
      value={id}
    >{name}</Select.Option>;
  }), [optionData, fieldKeys]);
  const keys = useMemo(() => fieldKeys.keys, [fieldKeys]);

  return <Fragment>
    {map(keys, (key, index) => (<SimpleSelect
      key={key}
      uid={key}
      form={form}
      options={options}
      removeable={index > 0 || (keys && keys.length > 1)}
      onDelete={remove}
      label={label}
    />))}
    <FormItem>
      <Button
        icon="add"
        type="primary"
        funcType="flat"
        disabled={options.length <= 1}
        onClick={add}
      >
        {addText}
      </Button>
    </FormItem>
  </Fragment>;
}

DynamicSelect.propTypes = {
  optionData: PropTypes.array,
  fieldKeys: PropTypes.shape({
    keys: PropTypes.array,
  }),
  label: PropTypes.string,
  addText: PropTypes.string,
};

DynamicSelect.defaultProps = {
  label: '',
  addText: '',
};
