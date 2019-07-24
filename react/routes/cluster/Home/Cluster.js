import React, { Component } from 'react/index';
import { Button, Input, Form, Tooltip, Modal, Popover, Table, Tag, Icon, Radio, Pagination, Card } from 'choerodon-ui';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import CopyToBoard from 'react-copy-to-clipboard';
import LoadingBar from '../../../components/loadingBar';
import ClusterList from '../clusterList';
import './Cluster.scss';
import '../../envPipeline/EnvPipeLineHome.scss';
import '../../main.scss';
import '../../../components/DepPipelineEmpty/DepPipelineEmpty.scss';
import InterceptMask from '../../../components/interceptMask/InterceptMask';

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

const { AppState } = stores;
const Sidebar = Modal.Sidebar;
const RadioGroup = Radio.Group;
const FormItem = Form.Item;
const { TextArea } = Input;

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

@observer
class Cluster extends Component {
  /**
   * 检查编码是否合法
   * @param rule
   * @param value
   * @param callback
   */
  checkCode = _.debounce((rule, value, callback) => {
    const { ClusterStore, intl: { formatMessage } } = this.props;
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      ClusterStore.checkCode(this.state.organizationId, value)
        .then((error) => {
          if (error && error.failed) {
            callback(formatMessage({ id: 'envPl.code.check.exist' }));
          } else {
            callback();
          }
        });
    } else if (value && !pa.test(value)) {
      callback(formatMessage({ id: 'envPl.code.check.failed' }));
    } else {
      callback();
    }
  }, 1000);

  /**
   * 检查名称唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const { ClusterStore: { checkName, getClsData: clsData }, intl: { formatMessage } } = this.props;
    const { organizationId } = this.state;
    if ((clsData && value !== clsData.name) || !clsData) {
      checkName(organizationId, value)
        .then((data) => {
          if (data && data.failed) {
            callback(formatMessage({ id: 'template.checkName' }));
          } else {
            callback();
          }
        }).catch((error) => {
        Choerodon.prompt(error.response.data.message);
      });
    } else {
      callback();
    }
  }, 600);

  constructor(props) {
    super(props);
    const { organizationId } = AppState.currentMenuType;
    this.state = {
      id: '',
      organizationId,
      show: false,
      showDel: false,
      submitting: false,
      btnLoading: false,
      checked: true,
      sideType: '',
      createSelectedRowKeys: [],
      createSelected: [],
      selected: [],
      createSelectedTemp: [],
      token: null,
      delId: null,
      delCode: null,
      clsName: '',
      page: 1,
      size: 10,
    };
  }

  componentDidMount() {
    this.loadCluster();
  }

  handleRefresh = () => {
    this.loadCluster(this.state.page, this.state.size);
  };

  loadCluster = (page, size) => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    ClusterStore.loadCluster(organizationId, page, size);
  };

  onCreateSelectChange = (keys, selected) => {
    let s = [];
    const a = this.state.createSelectedTemp.concat(selected);
    this.setState({ createSelectedTemp: a });
    _.map(keys, o => {
      if (_.filter(a, ['id', o]).length) {
        s.push(_.filter(a, ['id', o])[0]);
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
    const { ClusterStore } = this.props;
    const {
      getTagKeys: tagKeys,
    } = ClusterStore;
    let s = [];
    const a = tagKeys.length ? tagKeys.concat(selected) : this.state.selected.concat(selected);
    this.setState({ selected: a });
    _.map(keys, o => {
      if (_.filter(a, ['id', o]).length) {
        s.push(_.filter(a, ['id', o])[0]);
      }
    });
    ClusterStore.setTagKeys(s);
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
  tableChange = (pagination, filters, sorter, paras) => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    ClusterStore.setInfo({ filters, sort: sorter, paras });
    let sort = { field: '', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let page = pagination.current;
    const postData = [paras.toString()];
    if (this.state.sideType === 'create') {
      ClusterStore.loadPro(organizationId, null, page, pagination.pageSize, sort, postData);
    } else {
      const id = ClusterStore.getClsData.id;
      ClusterStore.loadPro(organizationId, id, page, pagination.pageSize, sort, postData);
    }
  };

  /**
   * 辅助函数
   */
  handleCopy = () => {
    const { intl: { formatMessage } } = this.props;
    this.setState({ copyMsg: formatMessage({ id: 'envPl.token.coped' }) });
  };

  mouseEnter = () => {
    const { intl: { formatMessage } } = this.props;
    this.setState({ copyMsg: formatMessage({ id: 'envPl.code.copy.tooltip' }) });
  };

  delCluster = () => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    const clusters = ClusterStore.getData;
    this.setState({
      btnLoading: true,
    });
    ClusterStore.delCluster(organizationId, this.state.delId)
      .then((data) => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
          this.setState({
            btnLoading: false,
          });
        } else {
          this.setState({
            delId: null,
            showDel: false,
            btnLoading: false,
          }, () => {
            if (clusters.length % this.state.size === 1) {
              this.loadCluster(this.state.page - 1, this.state.size);
            } else {
              this.loadCluster(this.state.page, this.state.size);
            }
          });
        }
      })
      .catch(e => {
        this.setState({
          btnLoading: false,
        });
        Choerodon.handleResponseError(e);
      });
  };

  /**
   * 展开更多节点
   * @param id
   */
  showMore = (id) => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    const nodePageInfo = ClusterStore.getNodePageInfo;

    let page = 1;
    let size = 10;
    if (nodePageInfo[id]) {
      page = nodePageInfo[id].current;
      size = nodePageInfo[id].pageSize;
    }

    ClusterStore.loadMoreNode(organizationId, id, page, size);
  };

  delClusterShow = (id, name, code) => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;

    ClusterStore.clusterWithEnc(organizationId, id).then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setState({
          delId: id,
          showDel: true,
          clsName: name,
          delCode: code,
        });
      }
    });
  };

  getClusterList = () => {
    const { ClusterStore } = this.props;
    const { getData: clusters, getNodeData } = ClusterStore;

    return _.map(clusters, (cluster) => {
      const { id, connect, nodes } = cluster;
      const nodeData = nodes && nodes.list ? nodes.list.slice() : [];
      const tableData = getNodeData[id] && getNodeData[id].length ? getNodeData[id].slice() : nodeData;

      return <ClusterList
        key={id}
        clusterId={id}
        data={cluster}
        tableData={connect ? tableData : []}
        store={ClusterStore}
        showMore={this.showMore}
        delClusterShow={this.delClusterShow}
        showSideBar={this.showSideBar}
      />;
    });
  };

  getFormContent = () => {
    const {
      ClusterStore,
      intl: { formatMessage },
      form: { getFieldDecorator },
    } = this.props;
    const {
      getInfo: { filters, sort: { columnKey, order }, paras },
      getPageInfo,
      getProData: proData,
      getClsData: clsData,
      getShell: shell,
      getTagKeys: tagKeys,
      getTableLoading: tableLoading,
    } = ClusterStore;
    const { copyMsg, token, sideType, checked, createSelectedRowKeys, createSelected } = this.state;
    const rowCreateSelection = {
      selectedRowKeys: createSelectedRowKeys,
      onChange: this.onCreateSelectChange,
    };
    const rowSelection = {
      selectedRowKeys: _.map(tagKeys, s => s.id),
      onChange: this.onSelectChange,
    };
    const tagCreateDom = _.map(createSelected, t => <Tag className="c7n-env-tag" key={t.id}>{t.name} {t.code}</Tag>);
    const tagDom = _.map(tagKeys, (t) => {
      if (t) {
        return <Tag className="c7n-env-tag" key={t.id}>{t.name} {t.code}</Tag>;
      }
      return null;
    });
    const suffix = (<Tooltip placement="right" trigger="hover" title={copyMsg}>
      <div onMouseEnter={this.mouseEnter}>
        <CopyToBoard text={token} onCopy={this.handleCopy}>
          <i className="icon icon-library_books" />
        </CopyToBoard>
      </div>
    </Tooltip>);
    const suffix_key = (<Tooltip placement="right" trigger="hover" title={copyMsg}>
      <div onMouseEnter={this.mouseEnter}>
        <CopyToBoard text={shell} onCopy={this.handleCopy}>
          <i className="icon icon-library_books" />
        </CopyToBoard>
      </div>
    </Tooltip>);
    const columns = [{
      key: 'name',
      title: formatMessage({ id: 'cluster.project.name' }),
      dataIndex: 'name',
    }, {
      key: 'code',
      title: formatMessage({ id: 'cluster.project.code' }),
      dataIndex: 'code',
    }];

    let formContent = null;
    switch (sideType) {
      case 'create':
        formContent = (<div>
          <Form className="c7n-sidebar-form" layout="vertical">
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('code', {
                rules: [{
                  required: true,
                  message: formatMessage({ id: 'required' }),
                }, {
                  validator: this.checkCode,
                }],
              })(
                <Input
                  maxLength={30}
                  label={<FormattedMessage id="cluster.code" />}
                />,
              )}
            </FormItem>
            <FormItem
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
                  maxLength={10}
                  label={<FormattedMessage id="cluster.name" />}
                />,
              )}
            </FormItem>
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('description')(
                <TextArea
                  autosize={{ minRows: 2 }}
                  maxLength={30}
                  label={<FormattedMessage id="cluster.des" />}
                />,
              )}
            </FormItem>
          </Form>
          <div className="c7n-env-tag-title">
            <FormattedMessage id="cluster.authority" />
            <Popover
              overlayStyle={{ maxWidth: '350px' }}
              content={formatMessage({ id: 'cluster.authority.help' })}
            >
              <Icon type="help" />
            </Popover>
          </div>
          <div className="c7n-cls-radio">
            <RadioGroup label={<FormattedMessage id="cluster.public" />}
                        onChange={this.cbChange} value={checked}>
              <Radio value={true}><FormattedMessage id="cluster.project.all" /></Radio>
              <Radio value={false}><FormattedMessage id="cluster.project.part" /></Radio>
            </RadioGroup>
          </div>
          {checked ? null : <div>
            <div className="c7n-sidebar-form">
              <Table
                rowSelection={rowCreateSelection}
                columns={columns}
                dataSource={proData}
                filterBarPlaceholder={formatMessage({ id: 'filter' })}
                pagination={getPageInfo}
                loading={tableLoading}
                onChange={this.tableChange}
                rowKey={record => record.id}
                filters={paras.slice()}
              />
            </div>
            <div className="c7n-env-tag-title">
              <FormattedMessage id="cluster.authority.project" />
            </div>
            <div className="c7n-env-tag-wrap">
              {tagCreateDom}
            </div>
          </div>}
        </div>);
        break;
      case 'token':
        formContent = (<div className="c7n-env-token c7n-sidebar-form">
          <div className="c7n-env-shell-wrap">
            <TextArea
              label={<FormattedMessage id="envPl.token" />}
              className="c7n-input-readOnly"
              autosize
              copy="true"
              readOnly
              value={token || ''}
            />
            <span className="c7n-env-copy">{suffix}</span>
          </div>
        </div>);
        break;
      case 'key':
        formContent = (<div className="c7n-env-token c7n-sidebar-form">
          <div className="c7n-env-shell-wrap">
            <TextArea
              label={<FormattedMessage id="envPl.token" />}
              className="c7n-input-readOnly"
              autosize
              copy="true"
              readOnly
              value={shell || ''}
            />
            <span className="c7n-env-copy">{suffix_key}</span>
          </div>
        </div>);
        break;
      case 'edit':
        formContent = (<div>
          <Form className="c7n-sidebar-form">
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('name', {
                rules: [{
                  required: true,
                  message: formatMessage({ id: 'required' }),
                }, {
                  validator: this.checkName,
                }],
                initialValue: clsData ? clsData.name : '',
              })(
                <Input
                  maxLength={10}
                  label={<FormattedMessage id="cluster.name" />}
                />,
              )}
            </FormItem>
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('description', {
                initialValue: clsData ? clsData.description : '',
              })(
                <TextArea
                  autosize={{ minRows: 2 }}
                  maxLength={30}
                  label={<FormattedMessage id="cluster.des" />}
                />,
              )}
            </FormItem>
          </Form>
          <div className="c7n-env-tag-title">
            <FormattedMessage id="cluster.authority" />
            <Popover
              overlayStyle={{ maxWidth: '350px' }}
              content={formatMessage({ id: 'cluster.authority.help' })}
            >
              <Icon type="help" />
            </Popover>
          </div>
          <div className="c7n-cls-radio">
            <RadioGroup label={<FormattedMessage id="cluster.public" />}
                        onChange={this.cbChange} value={checked}>
              <Radio value={true}><FormattedMessage id="cluster.project.all" /></Radio>
              <Radio value={false}><FormattedMessage id="cluster.project.part" /></Radio>
            </RadioGroup>
          </div>
          {checked ? null : <div>
            <div className="c7n-sidebar-form">
              <Table
                rowSelection={rowSelection}
                columns={columns}
                dataSource={proData}
                filterBarPlaceholder={formatMessage({ id: 'filter' })}
                pagination={getPageInfo}
                loading={tableLoading}
                onChange={this.tableChange}
                rowKey={record => record.id}
                filters={paras.slice()}
              />
            </div>
            <div className="c7n-env-tag-title">
              <FormattedMessage id="cluster.authority.project" />
            </div>
            <div className="c7n-env-tag-wrap">
              {tagDom}
            </div>
          </div>}
        </div>);
        break;
      default:
        formContent = null;
    }
    return formContent;
  };

  /**
   * 提交数据
   * @param e
   */
  handleSubmit = (e) => {
    e.preventDefault();
    const { ClusterStore } = this.props;
    const { organizationId, sideType, checked, createSelectedRowKeys } = this.state;
    const tagKeys = ClusterStore.getTagKeys;
    this.setState({
      submitting: true,
    });
    if (sideType === 'create') {
      this.props.form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          this.setState({ clsName: data.name });
          data.skipCheckProjectPermission = checked;
          data.projects = createSelectedRowKeys;
          ClusterStore.createCluster(organizationId, data)
            .then((res) => {
              if (res) {
                if (res && res.failed) {
                  this.setState({
                    submitting: false,
                  });
                  Choerodon.prompt(res.message);
                } else {
                  this.loadCluster();
                  this.setState({
                    sideType: 'token',
                    token: res,
                    submitting: false,
                    createSelectedRowKeys: [],
                    createSelected: [],
                  });
                }
              }
            })
            .catch(e => {
              this.setState({
                submitting: false,
              });
              Choerodon.handleResponseError(e);
            });
        } else {
          this.setState({
            submitting: false,
          });
        }
      });
    } else if (sideType === 'edit') {
      const id = ClusterStore.getClsData.id;
      const proIds = _.map(tagKeys, t => t.id);
      this.props.form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          data.skipCheckProjectPermission = checked;
          data.projects = proIds;
          ClusterStore.updateCluster(organizationId, id, data)
            .then((res) => {
              if (res && res.failed) {
                this.setState({
                  submitting: false,
                });
                Choerodon.prompt(res.message);
              } else {
                ClusterStore.setTagKeys([]);
                this.loadCluster();
                this.setState({ show: false, submitting: false });
              }
            })
            .catch(e => {
              this.setState({
                submitting: false,
              });
              Choerodon.handleResponseError(e);
            });
        } else {
          this.setState({
            submitting: false,
          });
        }
      });
    }
  };

  /**
   * 关闭侧边栏
   */
  handleCancelFun = () => {
    const { ClusterStore } = this.props;
    if (this.state.sideType === 'token') {
      this.loadCluster();
    }
    this.setState({ checked: true, show: false, createSelectedRowKeys: [], createSelected: [] });
    ClusterStore.setClsData(null);
    ClusterStore.setInfo({
      filters: {}, sort: { columnKey: 'id', order: 'descend' }, paras: [],
    });
    this.props.form.resetFields();
  };

  /**
   * 弹出侧边栏
   * @param sideType
   * @param id
   * @param name
   */
  showSideBar = (sideType, id, name) => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    if (sideType === 'create') {
      this.setState({ checked: true });
      ClusterStore.loadPro(organizationId, null, 0, HEIGHT <= 900 ? 10 : 15);
    } else if (sideType === 'edit') {
      ClusterStore.loadClsById(organizationId, id)
        .then((data) => {
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          } else {
            this.setState({ checked: data.skipCheckProjectPermission });
          }
        });
      ClusterStore.loadPro(organizationId, id, 0, HEIGHT <= 900 ? 10 : 15);
      ClusterStore.loadTagKeys(organizationId, id);
    } else if (sideType === 'key') {
      ClusterStore.loadShell(organizationId, id);
    }
    this.setState({ sideType, show: true, clsName: name });
  };

  /**
   * 根据type显示右侧框标题
   * @returns {*}
   */
  showTitle = (sideType) => {
    if (sideType === 'create') {
      return <FormattedMessage id="cluster.create" />;
    } else if (sideType === 'edit') {
      return <FormattedMessage id="cluster.edit" />;
    } else if (sideType === 'permission') {
      return <FormattedMessage id="cluster.authority" />;
    } else {
      return <FormattedMessage id="cluster.active" />;
    }
  };

  /**
   * 根据type显示footer text
   * @param type
   * @returns {*}
   */
  okText = (type) => {
    const { intl: { formatMessage } } = this.props;
    if (type === 'create' || type === 'createGroup') {
      return formatMessage({ id: 'create' });
    } else if (type === 'edit' || type === 'editGroup' || type === 'permission') {
      return formatMessage({ id: 'save' });
    } else {
      return formatMessage({ id: 'envPl.close' });
    }
  };

  /**
   * 页码改变的回调
   * @param page
   * @param size
   */
  onPageChange = (page, size) => {
    this.setState({ page, size });
    this.loadCluster(page, size);
  };

  render() {
    const { type, organizationId, name } = AppState.currentMenuType;
    const { show, sideType, submitting, showDel, btnLoading, clsName, delCode } = this.state;
    const { ClusterStore, intl: { formatMessage } } = this.props;
    const {
      getClsPageInfo: { current, total, pageSize },
      getLoading: loading,
      getData: clusters,
    } = ClusterStore;
    const showBtns = (sideType === 'create' || sideType === 'edit' || sideType === 'permission');
    const titleName = sideType === 'create' ? name : clsName;

    return (
      <Page
        service={[
          'devops-service.devops-cluster.listCluster',
          'devops-service.devops-cluster.create',
          'devops-service.devops-cluster.queryShell',
          'devops-service.devops-cluster.query',
          'devops-service.devops-cluster.deleteCluster',
          'devops-service.devops-cluster.update',
          'devops-service.devops-cluster.listClusterProjects',
          'devops-service.devops-cluster.pageProjects',
        ]}
        className="c7n-region"
      >
        <Header title={<FormattedMessage id="cluster.head" />}>
          <Permission
            service={['devops-service.devops-cluster.create']}
            type={type}
            organizationId={organizationId}
          >
            <Button
              icon="playlist_add"
              funcType="flat"
              onClick={this.showSideBar.bind(this, 'create')}
            >
              <FormattedMessage id="cluster.create" />
            </Button>
          </Permission>
          <Permission
            service={['devops-service.devops-cluster.listCluster']}
            type={type}
            organizationId={organizationId}
          >
            <Button
              icon="refresh"
              funcType="flat"
              onClick={this.handleRefresh}
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Permission>
        </Header>
        {clusters && clusters.length ? <Content code={clusters && clusters.length ? 'cluster' : ''} values={{ name }}>
          {loading ? <LoadingBar display /> : <React.Fragment>
            {this.getClusterList()}
            {clusters.length && (<div className="c7n-cls-pagination">
              <Pagination
                tiny={false}
                showSizeChanger
                showSizeChangerLabel={false}
                total={total || 0}
                current={current || 0}
                pageSize={pageSize || 0}
                onChange={this.onPageChange}
                onShowSizeChange={this.onPageChange}
              />
            </div>)}
          </React.Fragment>}
        </Content> : <Content>
          <Card title={formatMessage({ id: 'cluster.create' })} className="c7n-depPi-empty-card">
            <div className="c7n-noEnv-content">
              <FormattedMessage id="cluster.noData.text1" /><br />
              <FormattedMessage id="cluster.noData.text2" /><br />
              <FormattedMessage id="cluster.noData.text3" />
              <a
                href={formatMessage({ id: 'cluster.link' })}
                rel="nofollow me noopener noreferrer"
                target="_blank"
              >
                <FormattedMessage id="depPl.more" /><Icon type="open_in_new" />
              </a>
              <div className="c7n-cluster-notice">
                <Icon type="error" />
                <FormattedMessage id="cluster.notice" />
              </div>
            </div>
            <Button
              type="primary"
              funcType="raised"
              onClick={this.showSideBar.bind(this, 'create')}
            >
              <FormattedMessage id="cluster.create" />
            </Button>
          </Card>
        </Content>}
        {show && <Sidebar
          title={this.showTitle(sideType)}
          visible={show}
          onOk={(sideType === 'token' || sideType === 'key') ? this.handleCancelFun : this.handleSubmit}
          onCancel={this.handleCancelFun.bind(this)}
          confirmLoading={submitting}
          okCancel={showBtns}
          cancelText={<FormattedMessage id="cancel" />}
          okText={this.okText(sideType)}
        >
          <Content code={`cluster.${sideType}`} values={{ clsName: titleName }} className="sidebar-content">
            {this.getFormContent()}
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>}
        <Modal
          className="c7n-cls-del-modal"
          title={<FormattedMessage id="cluster.del.title" values={{ clsName }} />}
          visible={showDel}
          onOk={this.delCluster}
          closable={false}
          footer={[
            <Button
              key="back"
              onClick={() => this.setState({ delId: null, showDel: false })}
              disabled={btnLoading}
            >
              <FormattedMessage id="cancel" />
            </Button>,
            <Button key="submit" type="danger" loading={btnLoading} onClick={this.delCluster}>
              <FormattedMessage id="cluster.del.confirm" />
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id="cluster.delDes_1" />
            <div
              className="c7n-cls-shell-input"
            >
              <Input
                value={`helm del choerodon-cluster-agent-code ${delCode || ''} --purge`}
                readOnly
                copy
              />
            </div>
            <div className="c7n-notice-wrap_error">
              <Icon type="error" /><FormattedMessage id="cluster.delDes_2" />
            </div>
          </div>
        </Modal>
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(Cluster)));
