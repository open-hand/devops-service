import React, { Fragment } from 'react';
import { Input, Form, Row, Col } from 'choerodon-ui';

const { TextArea } = Input;
const FormItem = Form.Item;

export default (props) => {
  const { formatMessage, intlPrefix } = props;

  return (
    <Fragment>
      <Form>
        <FormItem><Input placeholder={formatMessage({ id: `${intlPrefix}.name` })} /> </FormItem>
        <FormItem><Input placeholder={formatMessage({ id: `${intlPrefix}.code` })} /> </FormItem>
        <FormItem><TextArea placeholder={formatMessage({ id: `${intlPrefix}.dec` })} /> </FormItem>
      </Form>
      
    </Fragment>);
};
