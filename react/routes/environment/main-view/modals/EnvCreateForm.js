import React from 'react';
import { Form, TextField, Select, TextArea } from 'choerodon-ui/pro';

const { Option } = Select;

export default function ({ dataSet }) {
  return <Form dataSet={dataSet}>
    <Select name="cluster">
      <Option value="zh-cn">简体中文</Option>
      <Option value="en-us">英语(美国)</Option>
      <Option value="ja-jp">日本語</Option>
    </Select>
    <TextField name="code" />
    <TextField name="name" />
    <TextArea name="description" />
    <Select name="group">
      <Option value="zh-cn">简体中文</Option>
      <Option value="en-us">英语(美国)</Option>
      <Option value="ja-jp">日本語</Option>
    </Select>
  </Form>;
}
