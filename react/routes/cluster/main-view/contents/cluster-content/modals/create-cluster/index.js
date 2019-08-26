import React, { Fragment, useState, useMemo } from 'react';
import { Input, Form, Row, Col } from 'choerodon-ui';
import _ from 'lodash';
import { Modal } from 'choerodon-ui/pro';

import { handlePromptError } from '../../../../../../../utils';
import ActivateCluster from '../activate-cluster';

const { TextArea } = Input;
const FormItem = Form.Item;

const formDataModal = {
  name: String,
  code: String,
  description: String,
};

const ActivateClusterModalKey = Modal.key();

const CreateCluster = (props) => {
  const [formData, setFormData] = useState(formDataModal);
  const { resreshTree, projectId, modalStore, formatMessage, intlPrefix, modal, form } = props;
  const { getFieldDecorator, validateFields } = form;
  modal.handleOk(() => {
    validateFields(async (err, values) => {
      if (!err) {
        const res = await modalStore.createCluster({ projectId, ...formData });
        if (handlePromptError(res, true)) {
          openActivate(res);
          resreshTree();
          modal.close();
        }
      }
    });
    return false;
  });


  const handleNameChange = ({ target: { value } }) => {
    handleChange({ name: value });
  };
  const handleCodeChange = ({ target: { value } }) => {
    handleChange({ code: value });
  };
  const handleDescriptionChange = ({ target: { value } }) => {
    handleChange({ description: value });
  };
  const handleChange = (newState) => {
    setFormData((oldState) => ({
      ...oldState,
      ...newState,
    }));
  };

  const openActivate = (cmd) => {
    Modal.open({
      key: ActivateClusterModalKey,
      title: formatMessage({ id: `${intlPrefix}.activate.header` }),
      children: <ActivateCluster cmd={cmd} intlPrefix={intlPrefix} formatMessage={formatMessage} modalStore={modalStore} projectId={projectId} />,
      drawer: true,
      style: {
        width: 500,
      },
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  };

  const checkName = useMemo(() => _.debounce((rule, value, callback) => {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      modalStore.checkClusterName({ projectId, clusterName: value })
        .then((res) => {
          if (res && res.failed) {
            callback(`名称${formatMessage({ id: `${intlPrefix}.check.exist` })}`);
          } else {
            callback();
          }
        })
        .catch((e) => {
          callback(`${formatMessage({ id: `${intlPrefix}.check.error` })}`);
        });
    } else if (value && !pa.test(value)) {
      callback(`名称${formatMessage({ id: `${intlPrefix}.check.failed` })}`);
    } else {
      callback();
    }
  }, 1000),
  []);
  const checkCode = useMemo(() => _.debounce((rule, value, callback) => {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      modalStore.checkClusterCode({ projectId, clusterCode: value })
        .then((res) => {
          if (res && res.failed) {
            callback(`编码${formatMessage({ id: `${intlPrefix}.check.exist` })}`);
          } else {
            callback();
          }
        })
        .catch((e) => {
          callback(`${formatMessage({ id: `${intlPrefix}.check.error` })}`);
        });
    } else if (value && !pa.test(value)) {
      callback(`编码${formatMessage({ id: `${intlPrefix}.check.failed` })}`);
    } else {
      callback();
    }
  }, 1000),
  []);

  return (
    <Fragment>
      <Form>
        <FormItem>
          {getFieldDecorator('name', {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: checkName,
              },
            ], 
          })(
            <Input maxLength={30} placeholder={formatMessage({ id: `${intlPrefix}.name` })} onChange={handleNameChange} />
          )} </FormItem>
        <FormItem>
          {getFieldDecorator('code', {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: checkCode,
              },
            ],
          })(
            <Input maxLength={10} placeholder={formatMessage({ id: `${intlPrefix}.code` })} onChange={handleCodeChange} />
          )} </FormItem>
        <FormItem><TextArea placeholder={formatMessage({ id: `${intlPrefix}.dec` })} onChange={handleDescriptionChange} /> </FormItem>
      </Form>
    </Fragment>);
};


export default Form.create({})(CreateCluster);
