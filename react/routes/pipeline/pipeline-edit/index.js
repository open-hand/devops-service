import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter, Prompt } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Icon, Form, Input, Select, Radio } from 'choerodon-ui';
import { Content, Header, Page } from '@choerodon/master';
import _ from 'lodash';
import Sidebar from 'choerodon-ui/lib/modal/Sidebar';
import StageCard from '../components/stageCard';
import StageCreateModal from '../components/stageCreateModal';
import { STAGE_FLOW_AUTO, STAGE_FLOW_MANUAL, TRIGGER_TYPE_AUTO, TRIGGER_TYPE_MANUAL } from '../components/Constants';
import InterceptMask from '../../../components/intercept-mask';
import EmptyPage from '../components/emptyPage';
import './index.less';
import '../../main.less';


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
export default class PipelineEdit extends Component {
  state = {
    triggerType: null,
    showCreate: false,
    prevId: null,
    submitLoading: false,
    promptDisplay: true,
  };

  componentDidMount() {
    const {
      PipelineCreateStore,
      AppState: {
        currentMenuType: { id },
      },
    } = this.props;
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
    PipelineCreateStore.setPipeline([]);
  }

  onSubmit = () => {
    this.setState({ promptDisplay: false });
    const {
      PipelineCreateStore,
      form: { validateFieldsAndScroll },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      refreshTable,
    } = this.props;
    const { objectVersionNumber, id, name, triggerType } = PipelineCreateStore.getPipeline;

    validateFieldsAndScroll(async (err, { users }) => {
      if (!err) {
        const { getStageList, getTaskList, getPipeline } = PipelineCreateStore;
        const pipelineStageVOs = _.map(getStageList, (item) => ({
          ...item,
          pipelineTaskVOs: getTaskList[item.tempId] || null,
        }));
        this.setState({ submitLoading: true });
        const result = await PipelineCreateStore
          .editPipeline(projectId, {
            id,
            objectVersionNumber,
            name,
            triggerType,
            pipelineUserRels: triggerType === STAGE_FLOW_MANUAL ? _.map(users, (item) => Number(item)) : null,
            pipelineStageVOs,
          })
          .catch((e) => {
            this.setState({ submitLoading: false });
            Choerodon.handleResponseError(e);
          });
        this.setState({ submitLoading: false });
        if (result && result.failed) {
          Choerodon.prompt(result.message);
        } else {
          refreshTable();
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
    const { PipelineCreateStore } = this.props;
    PipelineCreateStore.setEditVisible(false);
    // 解决页面尚未退出 但是数据已经为空的问题
    setTimeout(() => {
      PipelineCreateStore.setUser([]);
      PipelineCreateStore.clearStageList();
      PipelineCreateStore.setStageIndex(0);
      PipelineCreateStore.clearTaskList();
      PipelineCreateStore.clearTaskSettings();
      PipelineCreateStore.clearTaskIndex();
      PipelineCreateStore.setTrigger(STAGE_FLOW_AUTO);
      PipelineCreateStore.setPipeline([]);
    }, 150);
  };

  get renderPipelineDom() {
    const { PipelineCreateStore: { getStageList } } = this.props;
    if (getStageList.length === 1) {
      const { tempId, stageName } = getStageList[0] || {};
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
      intl: { formatMessage },
      form: { getFieldDecorator },
      PipelineCreateStore,
    } = this.props;
    const { triggerType, showCreate, prevId, submitLoading, promptDisplay } = this.state;
    const {
      getLoading,
      getUser,
      getIsDisabled,
      getPipeline,
      getDetailLoading,
      getCanSubmit,
      editVisible: visible,
    } = PipelineCreateStore;

    const showUserSelector = triggerType
      ? (triggerType === STAGE_FLOW_MANUAL)
      : (getPipeline.triggerType === STAGE_FLOW_MANUAL);

    const user = _.map(getUser, ({ id, realName, loginName }) => (
      <Option key={id} value={String(id)}>{realName || loginName}</Option>));
    const initUser = _.map(getPipeline.pipelineUserRels, (item) => String(item));

    return (
      <Sidebar
        className="c7n-region c7n-pipeline-creat"
        title={formatMessage({ id: 'pipeline.header.edit' }) + getPipeline.name}
        visible={visible}
        onOk={this.onSubmit}
        onCancel={this.goBack}
        okText={<FormattedMessage id="save" />}
        cancelText={<FormattedMessage id="cancel" />}
        confirmLoading={submitLoading}
        keyboard={false}
      >
        { _.isNull(getPipeline) ? <EmptyPage /> : <Fragment>
          <Content className="c7n-pipeline-content">
            <Form
              layout="vertical"
            >
              <FormItem
                className="c7n-select_512 c7ncd-formitem-bottom"
                {...formItemLayout}
              >
                {getFieldDecorator('triggerType', {
                  initialValue: getPipeline.triggerType,
                })(
                  <div>
                    <span className="radio-label"><FormattedMessage id="pipeline.trigger" />:</span>
                    <RadioGroup value={PipelineCreateStore.trigger} onChange={this.changeTriggerType}>
                      <Radio value={STAGE_FLOW_AUTO} disabled={getPipeline.triggerType}>
                        <FormattedMessage id="pipeline.trigger.auto" />
                      </Radio>
                      <Radio value={STAGE_FLOW_MANUAL} disabled={getPipeline.triggerType}>
                        <FormattedMessage id="pipeline.trigger.manual" />
                      </Radio>
                    </RadioGroup>
                  </div>
                )}
              </FormItem>
              {showUserSelector && <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator('users', {
                  rules: [{
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  }],
                  initialValue: initUser,
                })(
                  <Select
                    filter
                    allowClear
                    mode="multiple"
                    optionFilterProp="children"
                    label={formatMessage({ id: 'pipeline.trigger.member' })}
                    loading={getLoading.user}
                    getPopupContainer={(triggerNode) => triggerNode.parentNode}
                    filterOption={(input, option) => option.props.children
                      .toLowerCase()
                      .indexOf(input.toLowerCase()) >= 0}
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
                <span className="c7ncd-pipeline-error-msg">请检查任务类型是否正确！</span>
              </div>}
              {!getCanSubmit && <div className="c7ncd-pipeline-error">
                <Icon type="error" className="c7ncd-pipeline-error-icon" />
                <span className="c7ncd-pipeline-error-msg">{formatMessage({ id: 'pipeline.create.error-2' })}</span>
              </div>}
            </Form>
            <InterceptMask visible={submitLoading || getDetailLoading} />
          </Content>
          {showCreate && <StageCreateModal
            store={PipelineCreateStore}
            prevId={prevId}
            visible={showCreate}
            onClose={this.closeCreateForm}
          />}
        </Fragment>}
      </Sidebar>);
  }
}
