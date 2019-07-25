import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content } from '@choerodon/boot';
import _ from 'lodash';
import {
  Form,
  Input,
  Modal,
  Radio,
  Table,
  Tag,
  Button,
} from 'choerodon-ui';
import classnames from 'classnames';
import CertConfig from '../../components/certConfig';
import Tips from '../../components/Tips/Tips';
import InterceptMask from '../../components/interceptMask/InterceptMask';
import { handleCheckerProptError } from '../../utils';

import '../main.scss';
import './style/CertificateCreate.scss';
import '../envPipeline/EnvPipeLineHome.scss';

const { TextArea } = Input;
const { Sidebar } = Modal;
const { Item: FormItem } = Form;
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
@inject('AppState')
@observer
export default class CertificateCreate extends Component {
  state = {
    submitting: false,
    type: 'request',
    keyLoad: false,
    certLoad: false,
    checked: true,
    createSelectedRowKeys: [],
    createSelected: [],
    selected: [],
    createSelectedTemp: [],
    uploadMode: false,
  };

  /**
   * 校验证书名称唯一性
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      store,
      intl: { formatMessage },
      AppState: { currentMenuType: { organizationId } },
    } = this.props;

    store.checkCertName(organizationId, value)
      .then(data => {
        if (data && data.failed) {
          callback(formatMessage({ id: 'ctf.name.check.exist' }));
        } else {
          callback();
        }
      })
      .catch(() => callback());
  }, 1000);

  async componentDidMount() {
    const {
      showType,
      store,
      id,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;

    if (showType === 'edit') {
      store.loadProjects(organizationId, id);
      store.loadTagKeys(organizationId, id);

      const response = await store.loadCertById(organizationId, id)
        .catch(e => {
          Choerodon.handleResponseError(e);
        });

      if (handleCheckerProptError(response)) {
        this.setState({
          checked: response.skipCheckProjectPermission,
        });
      }
    } else {
      store.loadProjects(organizationId);
    }
  }

  handleSubmit = async e => {
    e.preventDefault();
    const {
      store,
      showType,
      AppState: { currentMenuType: { organizationId } },
      form: {
        validateFieldsAndScroll,
      },
    } = this.props;

    const {
      checked,
      createSelectedRowKeys,
      uploadMode,
    } = this.state;

    this.setState({ submitting: true });

    validateFieldsAndScroll(async (err, data) => {
      if (!err) {
        const submitFunc = {
          create: data => {
            const formData = new FormData();
            const excludeProps = ['domainArr', 'cert', 'key'];

            if (uploadMode) {
              const { key, cert } = data;

              formData.append('key', key.file);
              formData.append('cert', cert.file);
            }

            _.forEach(data, (value, k) => {
              if (!_.includes(excludeProps, k)) {
                formData.append(k, value);
              }
            });

            return store.createCert(organizationId, formData);
          },
          edit: (data) => {
            const { getCert, getTagKeys } = store;
            const proIds = _.map(getTagKeys, t => t.id);

            const _data = {
              ...data,
              projects: proIds,
            };

            return store.updateCert(organizationId, getCert.id, _data);
          },
        };

        const _data = {
          ...data,
          skipCheckProjectPermission: checked,
          projects: createSelectedRowKeys,
        };
        const request = submitFunc[showType];

        const result = await request(_data).catch(error => {
          Choerodon.handleResponseError(error);
          this.setState({ submitting: false });
        });

        this.setState({ submitting: false });

        if (handleCheckerProptError(result)) {
          store.initTableFilter();
          store.loadCertData(organizationId);

          this.handleClose();
        }
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  /**
   * 关闭弹框
   */
  handleClose = () => {
    const { onClose, store } = this.props;
    store.initProjectInfo();
    store.initProPageInfo();
    onClose();
  };

  /**
   * 域名格式检查
   * @param rule
   * @param value
   * @param callback
   */
  checkDomain = (rule, value, callback) => {
    const { intl: { formatMessage } } = this.props;
    const p = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (p.test(value)) {
      callback();
    } else {
      callback(formatMessage({ id: 'ctf.domain.check.failed' }));
    }
  };

  changeUploadMode = () => {
    this.setState({ uploadMode: !this.state.uploadMode });
  };

  onCreateSelectChange = (keys, selected) => {
    const { createSelectedTemp } = this.state;
    const allSelected = createSelectedTemp.concat(selected);

    let _selected = [];
    _.forEach(keys, key => {
      const user = _.find(allSelected, ['id', key]);
      if (user) {
        _selected.push(user);
      }
    });

    this.setState({
      createSelectedRowKeys: keys,
      createSelected: _selected,
    });
  };

  /**
   * 分配权限
   * @param keys
   * @param selected
   */
  onSelectChange = (keys, selected) => {
    const { store } = this.props;
    const {
      getTagKeys: tagKeys,
    } = store;

    const allSelected = tagKeys.length ? tagKeys.concat(selected) : this.state.selected.concat(selected);
    this.setState({ selected: allSelected });

    const _selected = [];
    _.forEach(keys, key => {
      const user = _.find(allSelected, ['id', key]);
      if (user) {
        _selected.push(user);
      }
    });

    store.setTagKeys(_selected);
  };

  cbChange = (e) => {
    this.setState({ checked: e.target.value });
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = ({ current, pageSize }, filters, sorter, param) => {
    const {
      store,
      showType,
      AppState: { currentMenuType: { organizationId } },
    } = this.props;

    const sort = _.isEmpty(sorter)
      ? {
        field: 'id',
        columnKey: 'id',
        order: 'descend',
      }
      : sorter;

    store.setProjectInfo({
      page: current,
      postData: [param.toString()],
      pageSize,
      filters,
      sorter: sort,
      param,
    });

    if (showType === 'create') {
      store.loadProjects(organizationId);
    } else {
      const id = store.getCert ? store.getCert.id : null;
      store.loadProjects(organizationId, id);
    }
  };

  get getProjectContent() {
    const {
      showType,
      intl: { formatMessage },
      store: {
        getProjectInfo: { param },
        getProPageInfo,
        getProData,
        getTagKeys: tagKeys,
        getTableLoading,
      },
    } = this.props;

    const {
      createSelectedRowKeys,
      createSelected,
    } = this.state;

    const rowCreateSelection = {
      selectedRowKeys: createSelectedRowKeys,
      onChange: this.onCreateSelectChange,
    };
    const rowSelection = {
      selectedRowKeys: _.map(tagKeys, s => s.id),
      onChange: this.onSelectChange,
    };

    const columns = [{
      key: 'name',
      title: formatMessage({ id: 'cluster.project.name' }),
      dataIndex: 'name',
    }, {
      key: 'code',
      title: formatMessage({ id: 'cluster.project.code' }),
      dataIndex: 'code',
    }];

    const tagCreateDom = _.map(createSelected, ({ id, name, code }) =>
      <Tag className="c7n-env-tag" key={id}>
        {name} {code}
      </Tag>,
    );

    const tagDom = _.map(tagKeys, t => t
      ? <Tag className="c7n-env-tag" key={t.id}>
        {t.name} {t.code}
      </Tag>
      : null,
    );

    return (
      <Fragment>
        <div className="c7n-sidebar-form">
          <Table
            noFilter
            rowSelection={showType === 'create' ? rowCreateSelection : rowSelection}
            columns={columns}
            dataSource={getProData}
            filterBarPlaceholder={formatMessage({ id: 'filter' })}
            pagination={getProPageInfo}
            loading={getTableLoading}
            onChange={this.tableChange}
            rowKey={record => record.id}
            filters={param.slice()}
          />
        </div>
        <div className="c7n-env-tag-title">
          <FormattedMessage id="cluster.authority.project" />
        </div>
        <div className="c7n-env-tag-wrap">
          {showType === 'create' ? tagCreateDom : tagDom}
        </div>
      </Fragment>
    );
  }

  render() {
    const {
      showType,
      intl: { formatMessage },
      form,
      AppState: { currentMenuType: { name } },
      store: { getCert },
    } = this.props;

    const { getFieldDecorator } = form;

    const {
      submitting,
      checked,
      uploadMode,
    } = this.state;

    const isCreateMode = showType === 'create';

    const panelClass = classnames({
      'c7n-creation-panel': isCreateMode,
    });

    const itemClass = classnames({
      'c7n-select_480': isCreateMode,
      'c7n-select_512': !isCreateMode,
    });

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          cancelText={<FormattedMessage id="cancel" />}
          okText={<FormattedMessage id={`${isCreateMode ? showType : 'save'}`} />}
          title={<FormattedMessage id={`ctf.sidebar.${showType}`} />}
          visible={showType !== ''}
          onOk={this.handleSubmit}
          onCancel={this.handleClose}
          confirmLoading={submitting}
        >
          <Content
            code={`certificate.${showType}`}
            values={{ name: getCert ? getCert.name : name }}
            className="c7n-ctf-create sidebar-content"
          >
            <Form layout="vertical">
              <FormItem className="c7n-select_512" {...formItemLayout}>
                {getFieldDecorator('name', {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                    {
                      validator: isCreateMode ? this.checkName : null,
                    },
                  ],
                  initialValue: getCert ? getCert.name : '',
                })(
                  <Input
                    maxLength={40}
                    type="text"
                    label={<FormattedMessage id="ctf.name" />}
                    disabled={showType === 'edit'}
                  />,
                )}
              </FormItem>
              {isCreateMode && <div className="c7n-creation-ctf-title">
                <FormattedMessage id="ctf.upload" />
              </div>}
              <div className={panelClass}>
                <FormItem
                  className={itemClass}
                  {...formItemLayout}
                >
                  {getFieldDecorator('domain', {
                    rules: [
                      {
                        required: true,
                        message: formatMessage({ id: 'required' }),
                      },
                      {
                        validator: isCreateMode ? this.checkDomain : null,
                      },
                    ],
                    initialValue: getCert ? getCert.domain : '',
                  })(
                    <Input
                      type="text"
                      maxLength={50}
                      label={<FormattedMessage id="ctf.config.domain" />}
                      disabled={showType === 'edit'}
                    />,
                  )}
                </FormItem>
                {isCreateMode && <Fragment>
                  <div className="c7n-creation-add-title">
                    <Tips
                      type="title"
                      data="certificate.file.add"
                      help={!uploadMode}
                    />
                    <Button
                      type="primary"
                      funcType="flat"
                      onClick={this.changeUploadMode}
                    >
                      <FormattedMessage id="ctf.upload.mode" />
                    </Button>
                  </div>
                  {CertConfig(uploadMode, form, formatMessage)}
                </Fragment>}
              </div>
              <div className="c7n-creation-ctf-title">
                <Tips type="title" data="certificate.permission" />
              </div>
              <div className="c7n-certificate-radio">
                <RadioGroup
                  label={<FormattedMessage id="certificate.public" />}
                  onChange={this.cbChange}
                  value={checked}
                >
                  <Radio value={true}><FormattedMessage id="cluster.project.all" /></Radio>
                  <Radio value={false}><FormattedMessage id="cluster.project.part" /></Radio>
                </RadioGroup>
              </div>
              {!checked && this.getProjectContent}
            </Form>
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}
