import React, { Component } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter, Prompt } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Icon, Form, Input, Select, Radio } from 'choerodon-ui';
import { Content, Header, Page } from '@choerodon/boot';
import _ from 'lodash';
import StageCard from '../components/stageCard';
import StageCreateModal from '../components/stageCreateModal';
import { STAGE_FLOW_AUTO, STAGE_FLOW_MANUAL, TRIGGER_TYPE_AUTO, TRIGGER_TYPE_MANUAL } from '../components/Constants';
import InterceptMask from '../../../components/interceptMask';
import Tips from '../../../components/Tips/Tips';

import './PipelineCreate.scss';
import '../../../main.scss';

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

@Form.create({})
@injectIntl
@withRouter
@inject('AppState')
@observer
export default class PipelineCreate extends Component {
  state = {
    triggerType: STAGE_FLOW_AUTO,
    showCreate: false,
    prevId: null,
    submitLoading: false,
    promptDisplay: true,
  };

  checkName = _.debounce((rule, value, callback) => {
    const {
      PipelineCreateStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const reg = /^\S+$/;
    const emojiMatch = /\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDE4F]/g;

    if (value) {
      if (reg.test(value) && !emojiMatch.test(value)) {
        PipelineCreateStore.checkName(projectId, value)
          .then(data => {
            if (data && data.failed) {
              callback(formatMessage({ id: 'checkNameExist' }));
            } else {
              callback();
            }
          })
          .catch(error => {
            callback(formatMessage({ id: 'checkNameFail' }));
          });
      } else {
        callback(formatMessage({ id: 'formatError' }));
      }
    } else {
      callback();
    }
  }, 600);

  componentDidMount() {
    const {
      PipelineCreateStore,
      AppState: {
        currentMenuType: { id },
      },
    } = this.props;

    PipelineCreateStore.loadUser(id);
    PipelineCreateStore.checkCanSubmit();
  }

  componentWillUnmount() {
    const { PipelineCreateStore } = this.props;
    PipelineCreateStore.setUser([]);
    PipelineCreateStore.clearStageList();
    PipelineCreateStore.setStageIndex(0);
    PipelineCreateStore.clearTaskList();
    PipelineCreateStore.clearTaskSettings();
    PipelineCreateStore.clearTaskIndex();
    PipelineCreateStore.setTrigger(STAGE_FLOW_AUTO);
  }

  onSubmit = (e) => {
    e.preventDefault();
    this.setState({ promptDisplay: false });

    const {
      PipelineCreateStore,
      form: { validateFieldsAndScroll },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    validateFieldsAndScroll(async (err, { name, triggerType, users }) => {
      if (!err) {
        const { getStageList, getTaskList } = PipelineCreateStore;
        const pipelineStageDTOS = _.map(getStageList, item => ({
          ...item,
          pipelineTaskDTOS: getTaskList[item.tempId] || null,
        }));
        this.setState({ submitLoading: true });
        const result = await PipelineCreateStore
          .createPipeline(projectId, {
            name,
            triggerType,
            pipelineUserRelDTOS: triggerType === STAGE_FLOW_MANUAL ? _.map(users, item => Number(item)) : null,
            pipelineStageDTOS,
          })
          .catch(e => {
            this.setState({ submitLoading: false });
            Choerodon.handleResponseError(e);
          });
        this.setState({ submitLoading: false });
        if (result && result.failed) {
          Choerodon.prompt(result.message);
        } else {
          this.goBack();
        }
      }
    });
  };

  changeTriggerType = (e) => {
    const { PipelineCreateStore } = this.props;
    const triggerType = e.target.value;
    this.setState({ triggerType });
    PipelineCreateStore.setTrigger(triggerType);

    if (triggerType === TRIGGER_TYPE_AUTO) {
      PipelineCreateStore.checkCanSubmit();
    } else {
      PipelineCreateStore.setCanSubmit(triggerType === TRIGGER_TYPE_MANUAL);
    }
  };

  /**
   * 创建阶段
   * @param prevId 该阶段的前置阶段的id
   */
  openCreateForm = (prevId) => {
    this.setState({ showCreate: true, prevId });
  };

  closeCreateForm = () => {
    this.setState({ showCreate: false, prevId: null });
  };

  goBack = () => {
    const { history } = this.props;
    history.go(-1);
  };

  get renderPipelineDom() {
    const { PipelineCreateStore: { getStageList } } = this.props;
    if (getStageList.length === 1) {
      const [{ tempId, stageName }] = getStageList;
      return <StageCard
        head
        allowDelete={false}
        key={tempId}
        stageId={tempId}
        stageName={stageName}
        clickAdd={this.openCreateForm}
      />;
    }

    return _.map(getStageList, ({ tempId, stageName }, stageIndex) => (<StageCard
      key={tempId}
      head={stageIndex === 0}
      stageId={tempId}
      stageName={stageName}
      clickAdd={this.openCreateForm}
    />));
  }

  render() {
    const {
      location: {
        pathname,
        search,
      },
      AppState: {
        currentMenuType: {
          name,
        },
      },
      intl: { formatMessage },
      form: { getFieldDecorator },
      PipelineCreateStore,
    } = this.props;
    const {
      triggerType,
      showCreate,
      prevId,
      submitLoading,
      promptDisplay,
    } = this.state;
    const {
      getLoading,
      getUser,
      getIsDisabled,
      getCanSubmit,
    } = PipelineCreateStore;

    const user = _.map(getUser, ({ id, realName, loginName }) => (
      <Option key={id} value={String(id)}>{realName || loginName}</Option>));

    return (<Page
      className="c7n-region c7n-pipeline-creat"
      service={[
        'devops-service.pipeline.create',
        'devops-service.pipeline.getAllUsers',
        'devops-service.pipeline.checkName',
        'devops-service.application.pageByOptions',
        'devops-service.devops-environment.listByProjectIdAndActive',
        'devops-service.application-instance.getByAppIdAndEnvId',
        'devops-service.pipeline-value.queryByAppIdAndEnvId',
        'devops-service.pipeline-value.queryById',
      ]}
    >
      <Prompt when={promptDisplay} message={formatMessage({ id: 'pipeline.before.leave' })} />
      <Header
        title={<FormattedMessage id="pipeline.header.create" />}
        backPath={`${pathname.replace(/\/create/, '')}${search}`}
      />
      <Content code="pipeline.create" values={{ name }}>
        <Form
          layout="vertical"
          onSubmit={this.onSubmit}
        >
          <FormItem
            className="c7n-select_512"
            {...formItemLayout}
          >
            {getFieldDecorator('name', {
              rules: [{
                required: true,
                message: formatMessage({ id: 'required' }),
              }, {
                validator: this.checkName,
              }],
            })(
              <Input
                autoFocus
                className="c7n-select_512"
                label={<FormattedMessage id="name" />}
                type="text"
                maxLength={30}
              />,
            )}
          </FormItem>
          <FormItem
            className="c7n-select_512 c7ncd-formitem-bottom"
            {...formItemLayout}
          >
            {getFieldDecorator('triggerType', {
              initialValue: STAGE_FLOW_AUTO,
            })(
              <RadioGroup
                label={<Tips type="title" data="pipeline.trigger" />}
                onChange={this.changeTriggerType}
              >
                <Radio value={STAGE_FLOW_AUTO}>
                  <FormattedMessage id="pipeline.trigger.auto" />
                </Radio>
                <Radio value={STAGE_FLOW_MANUAL}>
                  <FormattedMessage id="pipeline.trigger.manual" />
                </Radio>
              </RadioGroup>,
            )}
          </FormItem>
          {triggerType === STAGE_FLOW_MANUAL && <FormItem
            className="c7n-select_512"
            {...formItemLayout}
          >
            {getFieldDecorator('users', {
              rules: [{
                required: true,
                message: formatMessage({ id: 'required' }),
              }],
            })(
              <Select
                filter
                allowClear
                mode="multiple"
                optionFilterProp="children"
                label={formatMessage({ id: 'pipeline.trigger.member' })}
                loading={getLoading.user}
                getPopupContainer={triggerNode => triggerNode.parentNode}
                filterOption={(input, option) =>
                  option.props.children
                    .toLowerCase()
                    .indexOf(input.toLowerCase()) >= 0
                }
              >
                {user}
              </Select>,
            )}
          </FormItem>}
          <div className="c7ncd-pipeline-main">
            <div className="c7ncd-pipeline-scroll">
              {this.renderPipelineDom}
            </div>
          </div>
          {getIsDisabled && <div className="c7ncd-pipeline-error">
            <Icon type="error" className="c7ncd-pipeline-error-icon" />
            <span className="c7ncd-pipeline-error-msg">{formatMessage({ id: 'pipeline.create.error-1' })}</span>
          </div>}
          {!getCanSubmit && <div className="c7ncd-pipeline-error">
            <Icon type="error" className="c7ncd-pipeline-error-icon" />
            <span className="c7ncd-pipeline-error-msg">{formatMessage({ id: 'pipeline.create.error-2' })}</span>
          </div>}
          <FormItem
            {...formItemLayout}
          >
            <Button
              disabled={getIsDisabled || !getCanSubmit}
              type="primary"
              funcType="raised"
              htmlType="submit"
              loading={submitLoading}
            >
              <FormattedMessage id="create" />
            </Button>
            <Button
              disabled={submitLoading}
              funcType="raised"
              className="c7ncd-pipeline-btn_cancel"
              onClick={this.goBack}
            >
              <FormattedMessage id="cancel" />
            </Button>
          </FormItem>
        </Form>
        <InterceptMask visible={submitLoading} />
      </Content>
      {showCreate && <StageCreateModal
        store={PipelineCreateStore}
        prevId={prevId}
        visible={showCreate}
        onClose={this.closeCreateForm}
      />}
    </Page>);
  }

}
