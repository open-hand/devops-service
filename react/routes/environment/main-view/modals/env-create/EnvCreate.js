import React from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, Input } from 'choerodon-ui';
import StatusDot from '../../../../../components/status-dot';
import { useEnvFormStore } from '../env-form/stores';

const FormItem = Form.Item;
const { TextArea } = Input;
const { Option } = Select;
const formItemLayout = {
  labelCol: {
    xs: {
      span: 24,
    },
    sm: {
      span: 8,
    },
  },
  wrapperCol: {
    xs: {
      span: 24,
    },
    sm: {
      span: 16,
    },
  },
};

function EnvCreateForm({ modal }) {
  modal.handleOk(() => {
    // console.log(formDs.data);
  });

  function getClusterOption(record) {
    const id = record.get('id');
    const name = record.get('name');
    const connect = record.get('connect');

    return <Option key={id}>
      <StatusDot active synchronize connect={connect} />
      {name}
    </Option>;
  }

  return <Form className="c7n-sidebar-form" layout="vertical">
    <div className="c7ncd-sidebar-select">
      <FormItem {...formItemLayout}>
        {getFieldDecorator("clusterId", {
          rules: [
            {
              required: true,
              message: formatMessage({
                id: "required",
              }),
            },
          ],
        })(
          <Select
            allowClear={false}
            filter
            onSelect={this.handleCluster}
            filterOption={(input, option) =>
              option.props.children[1]
                .toLowerCase()
                .indexOf(input.toLowerCase()) >= 0
            }
            label={<FormattedMessage id="envPl.form.cluster" />}
          >
            {getCluster.length ? clusterOptions : null}
          </Select>
        )}
      </FormItem>
      <Tips type="form" data="envPl.cluster.tip" />
    </div>
    <FormItem {...formItemLayout}>
      {getFieldDecorator("code", {
        rules: [
          {
            required: true,
            message: formatMessage({
              id: "required",
            }),
          },
          {
            validator: this.checkCode,
          },
        ],
      })(
        <Input
          disabled={!getFieldValue("clusterId")}
          maxLength={30}
          label={<FormattedMessage id="envPl.form.code" />}
          suffix={<Tips type="form" data="envPl.envCode.tip" />}
        />
      )}
    </FormItem>
    <FormItem {...formItemLayout}>
      {getFieldDecorator("name", {
        rules: [
          {
            required: true,
            message: formatMessage({
              id: "required",
            }),
          },
        ],
      })(
        <Input
          disabled={!getFieldValue("clusterId")}
          maxLength={10}
          label={<FormattedMessage id="envPl.form.name" />}
          suffix={<Tips type="form" data="envPl.envName.tip" />}
        />
      )}
    </FormItem>
    <FormItem
      {...formItemLayout}
      label={<FormattedMessage id="envPl.form.description" />}
    >
      {getFieldDecorator("description")(
        <TextArea
          autosize={{
            minRows: 2,
          }}
          maxLength={60}
          label={<FormattedMessage id="envPl.form.description" />}
          suffix={<Tips type="form" data="envPl.chooseClu.tip" />}
        />
      )}
    </FormItem>
    <div className="c7ncd-sidebar-select">
      <FormItem {...formItemLayout}>
        {getFieldDecorator("devopsEnvGroupId")(
          <Select
            allowClear
            filter
            filterOption={(input, option) =>
              option.props.children
                .toLowerCase()
                .indexOf(input.toLowerCase()) >= 0
            }
            label={<FormattedMessage id="envPl.form.group" />}
          >
            {groupData.length
              ? _.map(groupData, g => (
                <Option key={g.id} value={g.id}>
                  {g.name}
                </Option>
              ))
              : null}
          </Select>
        )}
      </FormItem>
      <Tips type="form" data="envPl.group.tip" />
    </div>
  </Form>;
}

export default observer(EnvCreateForm);
