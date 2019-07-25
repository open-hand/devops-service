/**
 * @author ale0720@163.com
 * @date 2019-05-23 13:45
 */
import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import { Content } from '@choerodon/boot';
import {
  Table,
  Input,
  Form,
  Modal,
  Tooltip,
  Select,
  Icon,
  Popover,
  Radio,
  Tag,
} from 'choerodon-ui';
import Tips from '../../../components/Tips/Tips';
import InterceptMask from '../../../components/interceptMask/InterceptMask';
import '../../env-pipeline/index.scss';

const { Sidebar } = Modal;
const { Option } = Select;
const RadioGroup = Radio.Group;
const FormItem = Form.Item;
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
@inject('AppState')
@observer
export default class AppSidebar extends Component {
  postName = _.debounce((projectId, value, callback) => {
    const { AppStore, intl } = this.props;
    AppStore.checkName(projectId, value).then(data => {
      if (data && data.failed) {
        callback(intl.formatMessage({ id: 'template.checkName' }));
      } else {
        callback();
      }
    });
  }, 600);

  /**
   * 校验应用编码规则
   * @param rule
   * @param value
   * @param callback
   */
  checkCode = _.debounce((rule, value, callback) => {
    const {
      AppStore,
      intl: { formatMessage },
    } = this.props;
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      AppStore.checkCode(this.state.projectId, value).then(data => {
        if (data && data.failed) {
          callback(formatMessage({ id: 'template.checkCode' }));
        } else {
          callback();
        }
      });
    } else {
      callback(formatMessage({ id: 'template.checkCodeReg' }));
    }
  }, 600);

  state = {
    page: 1,
    id: '',
    projectId: menu.id,
    submitting: false,
    checked: true,
    selected: [],
    createSelectedRowKeys: [],
    createSelected: [],
    createSelectedTemp: [],
  };

  /**
   * 校验应用的唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = (rule, value, callback) => {
    const { AppStore, intl } = this.props;
    const singleData = AppStore.singleData;
    const pa = /^\S+$/;
    if (value && pa.test(value)) {
      if ((singleData && value !== singleData.name) || !singleData) {
        this.postName(this.state.projectId, value, callback);
      } else {
        callback();
      }
    } else {
      callback(intl.formatMessage({ id: 'app.checkName' }));
    }
  };

  /**
   * 提交数据
   * @param e
   */
  handleSubmit = e => {
    e.preventDefault();
    const { AppStore, form } = this.props;
    const { projectId, id, type, page, checked, createSelectedRowKeys } = this.state;
    const tagKeys = AppStore.getTagKeys;

    if (type === 'create') {
      form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const postData = data;
          postData.projectId = projectId;
          postData.isSkipCheckPermission = checked;
          postData.userIds = createSelectedRowKeys;

          this.setState({ submitting: true });
          AppStore.addData(projectId, postData)
            .then(res => {
              if (res && res.failed) {
                Choerodon.prompt(res.message);
                this.setState({ submitting: false });
              } else {
                this.loadAllData(page);
                AppStore.setMbrInfo({ filters: {}, sort: { columnKey: 'id', order: 'descend' }, paras: [] });

                this.setState({
                  type: false,
                  show: false,
                  submitting: false,
                  createSelectedRowKeys: [],
                  createSelected: [],
                  harborId: undefined,
                  chartId: undefined,
                });
              }
            })
            .catch(err => {
              this.setState({ submitting: false });
              Choerodon.handleResponseError(err);
            });
        }
      });
    } else if (type === 'edit') {
      form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const formData = data;
          const userIds = _.map(tagKeys, t => t.iamUserId);
          formData.isSkipCheckPermission = checked;
          formData.id = id;
          formData.userIds = userIds;

          this.setState({ submitting: true });
          AppStore.updateData(projectId, formData)
            .then(res => {
              if (res && res.failed) {
                Choerodon.prompt(res.message);
                this.setState({ submitting: false });
              } else {
                this.handleRefresh();

                AppStore.setMbrInfo({
                  filters: {},
                  sort: { columnKey: 'id', order: 'descend' },
                  paras: [],
                });

                this.setState(
                  {
                    show: false,
                    submitting: false,
                    harborId: undefined,
                    chartId: undefined,
                  }, () => {
                    AppStore.setTagKeys([]);
                  },
                );
              }
            })
            .catch(err => {
              this.setState({ submitting: false });
              Choerodon.handleResponseError(err);
            });
        }
      });
    }
  };

  selectTemplate = (value, option) => {
    this.setState({ copyFrom: option.key });
  };

  cbChange = e => {
    this.setState({ checked: e.target.value });
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  mbrTableChange = (pagination, filters, sorter, paras) => {
    const { AppStore } = this.props;
    const { id } = AppState.currentMenuType;
    AppStore.setMbrInfo({
      filters,
      sort: sorter,
      paras,
    });
    let sort = {
      field: '',
      order: 'desc',
    };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let searchParam = {};
    let page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    AppStore.loadPrm(id, page, pagination.pageSize, sort, postData);
  };

  onCreateSelectChange = (keys, selected) => {
    let s = [];
    const a = this.state.createSelectedTemp.concat(selected);
    this.setState({ createSelectedTemp: a });
    _.map(keys, o => {
      if (_.filter(a, ['iamUserId', o]).length) {
        s.push(_.filter(a, ['iamUserId', o])[0]);
      }
    });
    this.setState({
      createSelectedRowKeys: keys,
      createSelected: s,
    });
  };

  /**
   * 分配权限
   * @param keys
   * @param selected
   */
  onSelectChange = (keys, selected) => {
    const { AppStore } = this.props;
    const { getTagKeys: tagKeys } = AppStore;
    let s = [];
    const a = tagKeys.length
      ? tagKeys.concat(selected)
      : this.state.selected.concat(selected);
    this.setState({ selected: a });
    _.map(keys, o => {
      if (_.filter(a, ['iamUserId', o]).length) {
        s.push(_.filter(a, ['iamUserId', o])[0]);
      }
    });
    AppStore.setTagKeys(s);
  };

  getFormContent = () => {
    const {
      AppStore: {
        singleData,
        selectData,
        getMbrPageInfo,
        getTableLoading: tableLoading,
        getTagKeys: tagKeys,
        getMbr,
        getMbrInfo: { filters, paras: mbrParas },
        getHarborList,
        getChartList,
      },
      intl: { formatMessage },
      form: { getFieldDecorator },
    } = this.props;
    const {
      type: modeType,
      checked,
      createSelectedRowKeys,
      createSelected,
    } = this.state;

    const rowCreateSelection = {
      selectedRowKeys: createSelectedRowKeys,
      onChange: this.onCreateSelectChange,
    };

    const rowSelection = {
      selectedRowKeys: _.map(tagKeys, s => s.iamUserId),
      onChange: this.onSelectChange,
    };

    const tagCreateDom = _.map(createSelected, t => (
      <Tag className="c7n-env-tag" key={t.iamUserId}>
        {t.loginName} {t.realName}
      </Tag>
    ));

    const tagDom = _.map(tagKeys, t => {
      if (t) {
        return (
          <Tag className="c7n-env-tag" key={t.iamUserId}>
            {t.loginName} {t.realName}
          </Tag>
        );
      }
      return null;
    });

    const columns = [
      {
        key: 'loginName',
        filters: [],
        filteredValue: filters.loginName || [],
        title: formatMessage({
          id: 'envPl.loginName',
        }),
        dataIndex: 'loginName',
      },
      {
        key: 'realName',
        filters: [],
        filteredValue: filters.realName || [],
        title: formatMessage({
          id: 'envPl.userName',
        }),
        dataIndex: 'realName',
      },
    ];

    let initHarbor = undefined;
    if (getHarborList.length) {
      const hasProject = _.find(getHarborList, item => item.name.indexOf('project') !== -1);
      initHarbor = hasProject ? hasProject.id : getHarborList[0].id;
    }
    let initChart = getChartList.length ? getChartList[0].id : undefined;
    if (singleData && singleData.gitlabProjectId) {
      const { harborConfigId, chartConfigId } = singleData;
      const hasHarbor = _.find(getHarborList, ['id', harborConfigId]);
      const hasChart = _.find(getChartList, ['id', chartConfigId]);
      initHarbor = hasHarbor ? harborConfigId : undefined;
      initChart = hasChart ? chartConfigId : undefined;
    }
    return (<Fragment>
      <Form layout="vertical" className="c7n-sidebar-form">
        <div className="c7ncd-sidebar-select">
          <FormItem {...formItemLayout}>
            {getFieldDecorator('type', {
              initialValue: singleData ? singleData.type : 'normal',
            })(
              <Select
                key="service"
                label={<FormattedMessage id="app.chooseType" />}
                dropdownMatchSelectWidth
                disabled={modeType !== 'create'}
                size="default"
              >
                {['normal', 'test'].map(s => (
                  <Option value={s} key={s}>
                    <FormattedMessage id={`app.type.${s}`} />
                  </Option>
                ))}
              </Select>,
            )}
          </FormItem>
          <Tips type="form" data="app.chooseType.tip" />
        </div>
        {modeType === 'create' && (
          <FormItem {...formItemLayout}>
            {getFieldDecorator('code', {
              rules: [
                {
                  required: modeType === 'create',
                  whitespace: true,
                  max: 47,
                  message: formatMessage({ id: 'required' }),
                },
                {
                  validator: modeType === 'create' ? this.checkCode : null,
                },
              ],
            })(
              <Input
                maxLength={30}
                label={<FormattedMessage id="app.code" />}
                size="default"
                suffix={<Tips type="form" data="app.code.tooltip" />}
              />,
            )}
          </FormItem>
        )}
        <FormItem {...formItemLayout}>
          {getFieldDecorator('name', {
            rules: [
              {
                required: true,
                whitespace: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: this.checkName,
              },
            ],
            initialValue: singleData ? singleData.name : '',
          })(
            <Input
              maxLength={20}
              label={<FormattedMessage id="app.name" />}
              size="default"
            />,
          )}
        </FormItem>
        {modeType === 'create' && (
          <div className="c7ncd-sidebar-select">
            <FormItem {...formItemLayout}>
              {getFieldDecorator('applicationTemplateId', {
                rules: [
                  {
                    message: formatMessage({ id: 'required' }),
                    transform: value => {
                      if (value) {
                        return value.toString();
                      }
                      return value;
                    },
                  },
                ],
              })(
                <Select
                  filter
                  allowClear
                  label={<FormattedMessage id="app.chooseTem" />}
                  dropdownClassName="c7n-app-select-dropdown"
                  onSelect={this.selectTemplate}
                  filterOption={(input, option) =>
                    option.props.children.props.children
                      .toLowerCase()
                      .indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {selectData.map(s => (
                    <Option value={s.id} key={s.id}>
                      <Tooltip placement="right" title={s.description}>
                        {s.name}
                      </Tooltip>
                    </Option>
                  ))}
                </Select>,
              )}
            </FormItem>
            <Tips type="form" data="app.chooseTem.tip" />
          </div>
        )}
      </Form>
      {(singleData && singleData.gitlabProjectId || modeType === 'create') ? (<Fragment>
        <div className="c7n-env-tag-title">
          <FormattedMessage id="app.config" />
          <Popover
            overlayStyle={{ maxWidth: '350px' }}
            content={formatMessage({ id: 'app.config.help' })}
          >
            <Icon type="help" />
          </Popover>
        </div>
        <div className="c7n-app-config-panel">
          <FormItem
            className="c7n-select_480"
            {...formItemLayout}
          >
            {getFieldDecorator('harborConfigId', {
              initialValue: initHarbor,
              rules: [
                {
                  required: true,
                  message: formatMessage({ id: 'required' }),
                },
              ],
            })(
              <Select
                filter
                showSearch
                className="c7n-select_480"
                optionFilterProp="children"
                label={<FormattedMessage id="app.form.selectDocker" />}
                getPopupContainer={triggerNode => triggerNode.parentNode}
                filterOption={(input, option) =>
                  option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                }
              >
                {_.map(getHarborList, item => (<Option value={item.id} key={item.id}>
                  {item.name}
                </Option>))}
              </Select>,
            )}
          </FormItem>
          <FormItem
            className="c7n-select_480"
            {...formItemLayout}
          >
            {getFieldDecorator('chartConfigId', {
              initialValue: initChart,
              rules: [
                {
                  required: true,
                  message: formatMessage({ id: 'required' }),
                },
              ],
            })(
              <Select
                filter
                className="c7n-select_480"
                optionFilterProp="children"
                label={<FormattedMessage id="app.form.selectHelm" />}
                getPopupContainer={triggerNode => triggerNode.parentNode}
                filterOption={(input, option) =>
                  option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                }
              >
                {_.map(getChartList, item => (<Option value={item.id} key={item.id}>
                  {item.name}
                </Option>))}
              </Select>,
            )}
          </FormItem>
        </div>
        <div className="c7n-app-config-warn">
          <Icon type="error" className="c7n-app-config-warn-icon" />
          <FormattedMessage id="app.config.warn" />
        </div>
        <div className="c7n-env-tag-title">
          <FormattedMessage id="app.authority" />
          <Popover
            overlayStyle={{ maxWidth: '350px' }}
            content={formatMessage({ id: 'app.authority.help' })}
          >
            <Icon type="help" />
          </Popover>
        </div>
        <div className="c7n-app-authority-radio">
          <RadioGroup
            label={<FormattedMessage id="app.authority.label" />}
            onChange={this.cbChange}
            value={checked}
          >
            <Radio value={true}>
              <FormattedMessage id="app.mbr.all" />
            </Radio>
            <Radio value={false}>
              <FormattedMessage id="app.mbr.part" />
            </Radio>
          </RadioGroup>
        </div>
        {checked ? null : (
          <div>
            <div className="c7n-sidebar-form">
              <Table
                className="c7n-env-noTotal"
                rowSelection={
                  modeType === 'create' ? rowCreateSelection : rowSelection
                }
                columns={columns}
                dataSource={getMbr}
                filterBarPlaceholder={formatMessage({ id: 'filter' })}
                pagination={getMbrPageInfo}
                loading={tableLoading}
                onChange={this.mbrTableChange}
                rowKey={record => record.iamUserId}
                filters={mbrParas.slice()}
              />
            </div>
            <div className="c7n-env-tag-title">
              <FormattedMessage id="app.authority.mbr" />
            </div>
            <div className="c7n-env-tag-wrap">
              {modeType === 'create' ? tagCreateDom : tagDom}
            </div>
          </div>
        )}
      </Fragment>) : null}
    </Fragment>);
  };

  render() {
    const {
      store: {
        singleData,
      },
    } = this.props;
    const {
      type: modeType,
      show,
      submitting,
    } = this.state;

    const formContent = this.getFormContent();

    return (
      <Sidebar
        title={
          <FormattedMessage
            id={modeType === 'create' ? 'app.create' : 'app.edit'}
          />
        }
        visible={show}
        onOk={this.handleSubmit}
        okText={
          <FormattedMessage
            id={modeType === 'create' ? 'create' : 'save'}
          />
        }
        cancelText={<FormattedMessage id="cancel" />}
        confirmLoading={submitting}
        onCancel={this.hideSidebar}
      >
        <Content
          code={`app.${modeType}`}
          values={{ name: singleData ? singleData.name : name }}
          className="sidebar-content"
        >
          {formContent}
          <InterceptMask visible={submitting} />
        </Content>
      </Sidebar>
    );
  }
}
