import React, { Component, Fragment } from 'react';
import _ from 'lodash';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Radio, Table, Tag, Popover, Icon, Select, Form } from 'choerodon-ui';
import { STEP_FLAG } from '../Constants';

import '../AppImport.scss';
import '../../index.scss';

const RadioGroup = Radio.Group;
const FormItem = Form.Item;
const { Option } = Select;
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
class StepThree extends Component {
  state = {
    checked: 'all',
    selectedTemp: [],
    selectedRowKeys: [],
    selected: [],
  };

  componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      values,
    } = this.props;

    if (values) {
      this.setState({
        checked: values.isSkipCheckPermission || 'all',
        selectedRowKeys: values.userIds || [],
        selected: values.membersInfo || [],
      });
    }

    store.loadPrm(projectId);
    store.loadConfig(projectId);
  }

  onChange = e => this.setState({ checked: e.target.value });

  onSelectChange = (keys, selected) => {
    const { selectedTemp } = this.state;
    const allSelected = selectedTemp.concat(selected);

    let _selected = [];

    _.forEach(keys, key => {
      const user = _.find(allSelected, ['iamUserId', key]);
      if (user) {
        _selected.push(user);
      }
    });

    this.setState({
      selectedRowKeys: keys,
      selectedTemp: allSelected,
      selected: _selected,
    });
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  mbrTableChange = (pagination, filters, sorter, paras) => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    store.setMbrInfo({ filters, sort: sorter, paras });
    let sort = { field: '', order: 'desc' };
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
    store.loadPrm(projectId, page, pagination.pageSize, sort, postData);
  };

  handleNextStep = () => {
    const {
      onNext,
      store: {
        getHarborList,
        getChartList,
      },
      form,
    } = this.props;
    const { checked, selectedRowKeys, selected } = this.state;

    form.validateFields((err, { harborConfigId, chartConfigId }) => {
      if (!err) {
        const harborName = _.find(getHarborList, ['id', harborConfigId]).name;
        const chartName = _.find(getChartList, ['id', chartConfigId]).name;

        const values = {
          key: STEP_FLAG.PERMISSION_RULE,
          isSkipCheckPermission: checked,
          userIds: selectedRowKeys,
          membersInfo: selected,
          harborConfigId,
          chartConfigId,
          harborName,
          chartName,
        };

        onNext(values, STEP_FLAG.CONFORM_INFO);
      }
    });
  };

  render() {
    const {
      onPrevious,
      onCancel,
      store: {
        getMbrPageInfo,
        getTableLoading: tableLoading,
        getMbr,
        getMbrInfo: { filters, paras: mbrParas },
        getHarborList,
        getChartList,
      },
      intl: { formatMessage },
      form: { getFieldDecorator },
      values: {
        harborConfigId,
        chartConfigId,
      },
    } = this.props;
    const { checked, selectedRowKeys, selected } = this.state;

    const tagDom = _.map(selected, ({ iamUserId, loginName, realName }) => (
      <Tag className="c7n-import-tag" key={iamUserId}>
        {loginName} {realName}
      </Tag>
    ));

    const rowSelection = {
      selectedRowKeys,
      onChange: this.onSelectChange,
    };

    const defaultChart = getChartList.length ? getChartList[0].id : undefined;

    let defaultHarbor;
    if (getHarborList.length) {
      const privateHarbor = _.find(getHarborList, item => item.name.indexOf('project') !== -1);
      if (privateHarbor) {
        defaultHarbor = privateHarbor.id;
      } else {
        defaultHarbor = getHarborList[0].id;
      }
    }

    const harborOptions = _.map(getHarborList, ({ id, name }) => (<Option value={id} key={id}>
      {name}
    </Option>));

    const chartOptions = _.map(getChartList, ({ id, name }) => (<Option value={id} key={id}>
      {name}
    </Option>));

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

    return (
      <Fragment>
        <div className="steps-content-des">
          <FormattedMessage id="app.import.step2.des" />
        </div>
        <div className="steps-content-section">
          <div className="authority-radio">
            <RadioGroup
              label={<FormattedMessage id="app.authority.label" />}
              onChange={this.onChange}
              value={checked}
            >
              <Radio value={'all'}>
                <FormattedMessage id="app.mbr.all" />
              </Radio>
              <Radio value={'part'}>
                <FormattedMessage id="app.mbr.part" />
              </Radio>
            </RadioGroup>
          </div>
          {checked === 'all' ? null : (
            <div>
              <div className="c7n-sidebar-form">
                <Table
                  className="c7n-env-noTotal"
                  rowSelection={rowSelection}
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
              <div className="tag-title">
                <FormattedMessage id="app.authority.mbr" />
              </div>
              <div className="tag-wrap">
                {tagDom}
              </div>
            </div>
          )}
        </div>
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
          <Form layout="vertical" className="c7n-sidebar-form">
            <FormItem
              className="c7n-select_480"
              {...formItemLayout}
            >
              {getFieldDecorator('harborConfigId', {
                initialValue: harborConfigId || defaultHarbor,
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
                  {harborOptions}
                </Select>,
              )}
            </FormItem>
            <FormItem
              className="c7n-select_480"
              {...formItemLayout}
            >
              {getFieldDecorator('chartConfigId', {
                initialValue: chartConfigId || defaultChart,
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
                  {chartOptions}
                </Select>,
              )}
            </FormItem>
          </Form>
        </div>
        <div className="c7n-app-config-warn">
          <Icon type="error" className="c7n-app-config-warn-icon" />
          <FormattedMessage id="app.config.warn" />
        </div>
        <div className="steps-content-section">
          <Button
            type="primary"
            funcType="raised"
            onClick={this.handleNextStep}
          >
            <FormattedMessage id="next" />
          </Button>
          <Button
            onClick={() => onPrevious(STEP_FLAG.LANGUAGE_SELECT)}
            funcType="raised"
            className="c7n-btn-cancel"
          >
            <FormattedMessage id="previous" />
          </Button>
          <Button
            onClick={onCancel}
            funcType="raised"
            className="c7n-btn-cancel"
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </Fragment>
    );
  }
}

export default (injectIntl(StepThree));
