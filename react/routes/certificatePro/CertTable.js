import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Button, Popover, Tooltip } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import _ from 'lodash';
import { getTimeLeft, handleProptError } from '../../utils';
import MouserOverWrapper from '../../components/MouseOverWrapper';
import StatusIcon from '../../components/StatusIcon';
import EnvFlag from '../../components/envFlag';
import Tips from '../../components/Tips';
import DeleteModal from '../../components/deleteModal/DeleteModal';

import './style/CertTable.scss';

@injectIntl
@inject('AppState')
@observer
export default class CertTable extends Component {
  state = {
    deleteLoading: false,
    deleteArr: [],
  };

  componentWillUnmount() {
    const { store } = this.props;
    store.setCertData();
    store.initPageInfo();
    store.initTableFilter();
  }

  /**
   * 删除证书
   */
  handleDelete = async (id, callback) => {
    const {
      store,
      envId,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    this.setState({ deleteLoading: true });

    const response = await store.deleteCertById(projectId, id)
      .catch(err => {
        this.setState({ deleteLoading: false });
        callback && callback();
        Choerodon.handleResponseError(err);
      });

    const result = handleProptError(response);

    if (result) {

      this.removeDeleteModal(id);

      store.setTableFilter({ page: 1 });
      store.loadCertData(true, projectId, envId);
    }
    this.setState({ deleteLoading: false });
  };

  /**
   * 表格筛选排序等
   * @param pagination
   * @param filters
   * @param sorter
   * @param param
   */
  tableChange = ({ current, pageSize }, filters, sorter, param) => {
    const {
      store,
      envId,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    const sort = _.isEmpty(sorter)
      ? {
        field: 'id',
        columnKey: 'id',
        order: 'descend',
      }
      : sorter;

    const params = {
      page: current,
      pageSize,
      postData: {
        searchParam: filters,
        param: param.toString(),
      },
      sorter: sort,
      filters,
      param,
    };

    store.setTableFilter(params);
    store.loadCertData(true, projectId, envId);
  };

  /**
   * 显示删除确认框
   * @param id
   * @param name
   */
  openDeleteModal(id, name) {
    const deleteArr = [...this.state.deleteArr];

    const currentIndex = _.findIndex(deleteArr, item => id === item.deleteId);

    if (~currentIndex) {
      const newItem = {
        ...deleteArr[currentIndex],
        display: true,
      };
      deleteArr.splice(currentIndex, 1, newItem);
    } else {
      const newItem = {
        display: true,
        deleteId: id,
        name,
      };
      deleteArr.push(newItem);
    }

    this.setState({ deleteArr });
  }

  closeDeleteModal = (id) => {
    const deleteArr = [...this.state.deleteArr];

    const current = _.find(deleteArr, item => id === item.deleteId);

    current.display = false;

    this.setState({ deleteArr });
  };

  /**
   * 从当前模态框列表中移除已经完成的删除模态框
   * @param id
   */
  removeDeleteModal(id) {
    const { deleteArr } = this.state;
    const newDeleteArr = _.filter(deleteArr, ({ deleteId }) => deleteId !== id);
    this.setState({ deleteArr: newDeleteArr });
  }

  /**
   * 有效期
   * @param validFrom
   * @param validUntil
   * @param commandStatus
   * @returns {*}
   */
  validColumn = ({ validFrom, validUntil, commandStatus }) => {
    let msg = null;
    let content = null;
    if (!(validFrom && validUntil && commandStatus === 'success')) return content;

    content = (
      <Fragment>
        <FormattedMessage id="timeFrom" />：{validFrom}
        <br />
        <FormattedMessage id="timeUntil" />：{validUntil}
      </Fragment>
    );
    const start = new Date(validFrom.replace(/-/g, '/')).getTime();
    const end = new Date(validUntil.replace(/-/g, '/')).getTime();
    const now = Date.now();

    if (now < start) {
      msg = <FormattedMessage id="notActive" />;
    } else if (now > end) {
      msg = <FormattedMessage id="expired" />;
    } else {
      msg = getTimeLeft(now, end);
    }
    return (
      <Popover
        content={content}
        getPopupContainer={triggerNode => triggerNode.parentNode}
        trigger="hover"
        placement="top"
      >
        <span>{msg}</span>
      </Popover>
    );
  };

  renderActions = ({ id, domains, certName, commandStatus }) => {
    const {
      intl: { formatMessage },
    } = this.props;

    // NOTE: 域名返回一个数组
    //       约定数组的第一个元素为 CommonName
    //       之后的元素都是 DNSNames
    const detail = {
      CommonName: [domains[0]],
      DNSNames: domains.slice(1),
    };

    const btnProps = {
      shape: 'circle',
      size: 'small',
      funcType: 'flat',
    };
    const content = _.map(detail, (value, key) => value.length ? (
      <div className="c7n-overlay-content" key={value}>
        <p className="c7n-overlay-title">{key}</p>
        <div className="c7n-overlay-item">
          {_.map(value, item => (
            <p key={item} title={item} className="c7n-overlay-detail">
              {item}
            </p>
          ))}
        </div>
      </div>
    ) : null);
    return (
      <Fragment>
        <Popover
          overlayClassName="c7n-ctf-overlay"
          arrowPointAtCenter
          autoAdjustOverflow={false}
          title={formatMessage({ id: 'ctf.cert.detail' })}
          content={content}
          trigger="hover"
          placement="bottomRight"
        >
          <Button
            {...btnProps}
            icon="find_in_page"
          />
        </Popover>
        <Permission
          service={['devops-service.certification.delete']}
        >
          <Tooltip
            trigger="hover"
            placement="bottom"
            title={<FormattedMessage id="delete" />}
          >
            <Button
              {...btnProps}
              icon="delete_forever"
              onClick={() => this.openDeleteModal(id, certName)}
              disabled={commandStatus === 'operating'}
            />
          </Tooltip>
        </Permission>
      </Fragment>
    );
  };

  render() {
    const {
      intl: { formatMessage },
      store: {
        getTableFilter: {
          filters,
          sorter: { columnKey, order },
          param,
        },
        getCertLoading,
        getPageInfo,
        getCertData,
      },
    } = this.props;
    const {
      deleteLoading,
      deleteArr,
    } = this.state;

    const columns = [
      {
        title: <FormattedMessage id="ctf.column.name" />,
        key: 'certName',
        dataIndex: 'certName',
        filters: [],
        filteredValue: filters.certName || [],
        render: (text, { commandStatus, error }) => (
          <StatusIcon
            name={text}
            status={commandStatus || ''}
            error={error || ''}
          />
        ),
      },
      {
        title: <FormattedMessage id="ctf.column.ingress" />,
        key: 'domains',
        dataIndex: 'domains',
        filters: [],
        filteredValue: filters.domains || [],
        render: (text) => (
          <MouserOverWrapper text={text[0] || ''} width={0.25}>
            {text[0]}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="ctf.column.env" />,
        key: 'envName',
        sorter: true,
        filters: [],
        sortOrder: columnKey === 'envName' && order,
        filteredValue: filters.envName || [],
        render: ({ envConnected, envName }) => (
          <EnvFlag status={envConnected} name={envName} />
        ),
      },
      {
        title: <Tips type="title" data="validDate" />,
        key: 'valid',
        render: this.validColumn,
      },
      {
        align: 'right',
        width: 100,
        key: 'action',
        render: this.renderActions,
      },
    ];

    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'certificate.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="certificate"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <Fragment>
        <Table
          noFilter
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          onChange={this.tableChange}
          loading={getCertLoading}
          pagination={getPageInfo}
          dataSource={getCertData}
          filters={param.slice()}
          columns={columns}
          rowKey={record => record.id}
        />
        {deleteModals}
      </Fragment>
    );
  }
}
