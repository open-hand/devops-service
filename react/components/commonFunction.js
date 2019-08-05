import React from 'react';
import { stores } from '@choerodon/boot';
import _ from 'lodash';
import EnvOverviewStore from '../routes/envOverview/stores';
import DevopsStore from '../stores/DevopsStore';
import { handleProptError } from '../utils';

const { AppState } = stores;

export const commonComponent = storeName => {
  return component => class extends component {
    static displayName = 'commonComponent';

    clearAutoRefresh() {
      DevopsStore.clearAutoRefresh();
    }

    /**
     * 页面加载时开启自动刷新
     * @param {*} name
     */
    initAutoRefresh(name) {
      DevopsStore.initAutoRefresh(name, this.handleRefresh);
    }

    /**
     * 清空表格筛选状态
     */
    clearFilterInfo() {
      const store = this.props[storeName];
      store.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
    }

    /**
     * 加载table数据
     * @param page 加载第几页
     * @param envId 环境id
     * @param spin table加载动画
     * @param isRefresh 页面初始加载动画
     */
    loadAllData = (page, envId, spin = true, isRefresh = false) => {
      const store = this.props[storeName];
      const { id: projectId } = AppState.currentMenuType;
      store.loadData(spin, isRefresh, projectId, envId, page);
    };

    /**
     * 删除时需要进行二次校验
     * @param id
     * @param callback
     * @param hasValid 是否需要进行验证码校验
     * @returns {Promise<void>}
     */
    handleDelete = async (id, hasValid = false, callback) => {
      const store = this.props[storeName];
      const { id: projectId } = AppState.currentMenuType;

      this.setState({ submitting: true });

      const deleteId = hasValid ? id : this.state.id;

      const response = await store.deleteData(projectId, deleteId)
        .catch(error => {
          this.setState({ submitting: false });
          callback && callback();
          Choerodon.handleResponseError(error);
        });

      const result = handleProptError(response);

      if (result) {
        const { total, current, pageSize } = store.getPageInfo;
        const envId = EnvOverviewStore.getTpEnvId;

        // 防止最后一页只有一个元素时被删除后页面空白
        const lastCount = total % pageSize;
        const totalPage = Math.ceil(total / pageSize);
        if (lastCount === 1 && current === totalPage && current > 1) {
          this.loadAllData(current - 1, envId);
        } else {
          this.loadAllData(current, envId);
        }
        hasValid ? this.removeDeleteModal(id) : this.closeRemove();
      }
      this.setState({ submitting: false });
    };

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
        deleteArr.push({
          display: true,
          deleteId: id,
          name: name,
        });
      }

      this.setState({ deleteArr });
    }

    closeDeleteModal = (id) => {
      const deleteArr = [...this.state.deleteArr];

      const current = _.find(deleteArr, item => id === item.deleteId);

      current.display = false;

      this.setState({ deleteArr });
    };

    removeDeleteModal(id) {
      const { deleteArr } = this.state;
      const newDeleteArr = _.filter(deleteArr, ({ deleteId }) => deleteId !== id);
      this.setState({ deleteArr: newDeleteArr });
    }

    /**
     * 打开删除数据模态框
     * @param id 删除对象的id
     * @param name 删除对象的名称
     */
    openRemove = (id, name) => this.setState({ openRemove: true, id, name });

    /***
     * 关闭删除数据的模态框
     */
    closeRemove = () => this.setState({ openRemove: false, id: '', name: '' });

    /***
     * 处理刷新函数
     * @param loading 是否显示加载动画
     */
    handleRefresh = (loading = true) => {
      const store = this.props[storeName];
      const { filters, sort, paras } = store.getInfo;
      const pagination = store.getPageInfo;
      this.tableChange(pagination, filters, sort, paras, loading);
    };

    /**
     * table 操作
     * @param pagination
     * @param filters
     * @param sorter
     * @param paras
     * @param spin
     */
    tableChange = (pagination, filters, sorter, paras, spin = true) => {
      const store = this.props[storeName];
      const { id } = AppState.currentMenuType;
      const envId = EnvOverviewStore.getTpEnvId;
      store.setInfo({ filters, sort: sorter, paras });
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
      const { current, pageSize } = pagination;
      if (Object.keys(filters).length) {
        searchParam = filters;
      }
      const postData = {
        searchParam,
        param: paras.toString(),
      };
      store.loadData(
        spin,
        false,
        id,
        envId,
        current,
        pageSize,
        sort,
        postData,
      );
    };
  };
};
