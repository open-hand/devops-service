import React, { Component } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import _ from 'lodash';
import Sidebar from 'choerodon-ui/lib/modal/Sidebar';
import { Button, Modal, Form, Input, Select, Radio } from 'choerodon-ui';
import { Content } from '@choerodon/master';
import { STAGE_FLOW_MANUAL, STAGE_FLOW_AUTO } from '../Constants';
import '../../../main.scss';
import './StageCreateModal.scss';

const { Item: FormItem } = Form;
const { Option } = Select;
const { Group: RadioGroup } = Radio;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

@injectIntl
@Form.create({})
export default class StageCreateModal extends Component {
  static propTypes = {
    visible: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    prevId: PropTypes.number,
    stage: PropTypes.object,
    store: PropTypes.object,
  };

  static defaultProps = {
    stage: {},
  };

  state = {
    flowType: '',
  };

  changeFlowType = (e) => {
    this.setState({
      flowType: e.target.value,
    });
  };

  onSubmit = (e) => {
    e.preventDefault();
    const {
      store,
      prevId,
      onClose,
      stage,
      form: { validateFieldsAndScroll },
    } = this.props;

    validateFieldsAndScroll((err, { stageName, triggerType, users }) => {
      if (!err) {
        const data = {
          stageName,
          triggerType,
          stageUserRels: users ? _.map(users, (item) => Number(item)) : null,
        };
        if (_.isEmpty(stage)) {
          const currentIndex = store.getStageIndex + 1;
          store.addStage(prevId, {
            ...data,
            isParallel: 0,
            tempId: currentIndex,
          });
          store.setStageIndex(currentIndex);
        } else {
          store.editStage(stage.tempId, { ...stage, ...data });
        }
        onClose();
      }
    });
  };

  render() {
    const {
      intl: { formatMessage },
      form: { getFieldDecorator },
      stage: {
        stageName,
        triggerType,
        stageUserRels,
      },
      prevId,
      visible,
      onClose,
      store,
    } = this.props;
    const { flowType } = this.state;

    const createOrEdit = (prevId || prevId === 0) ? 'create' : 'edit';
    const user = _.map(store.getUser, ({ id, realName, loginName }) => (
      <Option key={id} value={String(id)}>{realName || loginName}</Option>));

    let initUsers;
    if (stageUserRels) {
      initUsers = _.map(stageUserRels.slice(), (item) => String(item));
    }

    return <Sidebar
      visible={visible}
      title={formatMessage({ id: `pipeline.stage.${createOrEdit}` })}
      width={400}
      onOk={this.onSubmit}
      onCancel={onClose}
      okText={<FormattedMessage id={createOrEdit} />}
      cancelText={<FormattedMessage id="cancel" />}
    >
      <Content style={{ height: '100%' }}>
        <Form layout="vertical">
          <FormItem
            {...formItemLayout}
          >
            {getFieldDecorator('stageName', {
              rules: [{
                required: true,
                message: formatMessage({ id: 'required' }),
              }],
              initialValue: stageName,
            })(
              <Input
                label={<FormattedMessage id="name" />}
                type="text"
                maxLength={30}
              />,
            )}
          </FormItem>
          <FormItem
            className="c7ncd-stage-modal-from"
            {...formItemLayout}
          >
            {getFieldDecorator('triggerType', {
              initialValue: triggerType || STAGE_FLOW_AUTO,
            })(
              <RadioGroup label={formatMessage({ id: 'pipeline.flow' })} onChange={this.changeFlowType}>
                <Radio value={STAGE_FLOW_AUTO}>
                  <FormattedMessage id="pipeline.flow.auto" />
                </Radio>
                <Radio value={STAGE_FLOW_MANUAL}>
                  <FormattedMessage id="pipeline.flow.manual" />
                </Radio>
              </RadioGroup>,
            )}
          </FormItem>
          {(flowType || triggerType) === STAGE_FLOW_MANUAL && <FormItem
            {...formItemLayout}
          >
            {getFieldDecorator('users', {
              rules: [{
                required: true,
                message: formatMessage({ id: 'required' }),
              }],
              initialValue: initUsers,
            })(
              <Select
                filter
                allowClear
                mode="multiple"
                optionFilterProp="children"
                label={formatMessage({ id: 'pipeline.flow.member' })}
                // getPopupContainer={(triggerNode) => triggerNode.parentNode}
                filterOption={(input, option) => option.props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0}
              >
                {user}
              </Select>,
            )}
          </FormItem>}
          {/* <FormItem
            className="c7ncd-stage-modal-btn"
            {...formItemLayout}
          >
            <Button key="back" onClick={onClose}>
              <FormattedMessage id="cancel" />
            </Button>
            <Button
              key="submit"
              type="primary"
              onClick={this.onSubmit}
            >
              <FormattedMessage id={createOrEdit} />
            </Button>
          </FormItem> */}
        </Form>
      </Content>
      
    </Sidebar>;
  }
}
