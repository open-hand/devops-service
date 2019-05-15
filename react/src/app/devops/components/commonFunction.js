import React from "react";
import { stores } from "@choerodon/boot";
import EnvOverviewStore from "../stores/project/envOverview";
import DevopsStore from "../stores/DevopsStore";

// const REFRESH_MANUAL = "manual";
// const REFRESH_AUTOMATIC = "auto";

const { AppState } = stores;

export const commonComponent = storeName => {
  return component =>
    class extends component {
      static displayName = "commonComponent";

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
          sort: { columnKey: "id", order: "descend" },
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
       * 打开删除数据模态框
       * @param id 删除对象的id
       * @param name 删除对象的名称
       */
      openRemove = (id, name) => this.setState({ openRemove: true, id, name });

      /***
       * 删除数据
       */
      handleDelete = () => {
        const store = this.props[storeName];
        const { id } = this.state;
        const { id: projectId } = AppState.currentMenuType;
        const { total, current, pageSize } = store.getPageInfo;
        const envId = EnvOverviewStore.getTpEnvId;

        // 防止最后一页只有一个元素时被删除后页面空白
        const lastCount = total % pageSize;
        const totalPage = Math.ceil(total / pageSize);

        this.setState({ submitting: true });

        store
          .deleteData(projectId, id)
          .then(data => {
            this.setState({ submitting: false });

            if (data) {
              if (lastCount === 1 && current === totalPage && current > 1) {
                this.loadAllData(current - 2, envId);
              } else {
                this.loadAllData(current - 1, envId);
              }
            }

            this.closeRemove();
          })
          .catch(error => {
            this.setState({ submitting: false });
            Choerodon.handleResponseError(error);
          });
      };

      /***
       * 关闭删除数据的模态框
       */

      closeRemove = () => this.setState({ openRemove: false });

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

      /***
       * 处理页面跳转
       * @param url 跳转地址
       */
      linkToChange = url => {
        const { history } = this.props;
        history.push(url);
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
        let sort = { field: "", order: "desc" };
        if (sorter.column) {
          sort.field = sorter.field || sorter.columnKey;
          if (sorter.order === "ascend") {
            sort.order = "asc";
          } else if (sorter.order === "descend") {
            sort.order = "desc";
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
          current - 1,
          pageSize,
          sort,
          postData
        );
      };

      /**
       * 获取屏幕的高度
       * @returns {number}
       */
      getHeight = () => {
        const HEIGHT =
          window.innerHeight ||
          document.documentElement.clientHeight ||
          document.body.clientHeight;
        let height = 310;
        if (HEIGHT <= 800) {
          height = 310;
        } else if (HEIGHT > 800 && HEIGHT <= 900) {
          height = 450;
        } else if (HEIGHT > 900 && HEIGHT <= 1050) {
          height = 600;
        } else {
          height = 630;
        }
        return height;
      };

      handleProptError = error => {
        if (error && error.failed) {
          Choerodon.prompt(error.message);
          return false;
        } else {
          return error;
        }
      };
    };
};
