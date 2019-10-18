import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import findIndex from 'lodash/findIndex';
import find from 'lodash/find';
import filter from 'lodash/filter';

export default function useStore() {
  return useLocalStore(() => ({
    navBounds: {},
    setNavBounds(data) {
      this.navBounds = data;
    },
    get getNavBounds() {
      return this.navBounds;
    },

    deleteArr: [],
    setDeleteArr(data) {
      this.deleteArr = data;
    },
    get getDeleteArr() {
      return this.deleteArr;
    },

    deleteCheck(projectId, envId, objectType) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/check_delete_resource?env_id=${envId}&object_type=${objectType}`);
    },

    sendMessage(projectId, envId, objectId, notificationId, objectType) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/send_message?env_id=${envId}&object_id=${objectId}&notification_id=${notificationId}&object_type=${objectType}`);
    },

    validateCaptcha(projectId, envId, objectId, captcha, objectType) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/validate_captcha?env_id=${envId}&object_id=${objectId}&captcha=${captcha}&object_type=${objectType}`);
    },

    openDeleteModal(envId, id, name, type, refresh) {
      const newDeleteArr = [...this.deleteArr];

      const currentIndex = findIndex(newDeleteArr, (item) => id === item.deleteId && type === item.type);

      if (currentIndex > -1) {
        const newItem = {
          ...newDeleteArr[currentIndex],
          display: true,
          refresh,
        };
        newDeleteArr.splice(currentIndex, 1, newItem);
      } else {
        const newItem = {
          display: true,
          deleteId: id,
          name,
          type,
          refresh,
          envId,
        };
        newDeleteArr.push(newItem);
      }
      this.setDeleteArr(newDeleteArr);
    },

    closeDeleteModal(id, type) {
      const newDeleteArr = [...this.deleteArr];
      const current = find(newDeleteArr, (item) => id === item.deleteId && type === item.type);
      current.display = false;
      this.setDeleteArr(newDeleteArr);
    },

    removeDeleteModal(id, type) {
      const newDeleteArr = filter(this.deleteArr, ({ deleteId, type: objectType }) => deleteId !== id || objectType !== type);
      this.setDeleteArr(newDeleteArr);
    },

    deleteData(projectId, id, type, envId) {
      const url = {
        instance: `/devops/v1/projects/${projectId}/app_service_instances/${id}/delete`,
        service: `/devops/v1/projects/${projectId}/service/${id}`,
        ingress: `/devops/v1/projects/${projectId}/ingress/${id}`,
        certificate: `/devops/v1/projects/${projectId}/certifications?cert_id=${id}`,
        configMap: `/devops/v1/projects/${projectId}/config_maps/${id}`,
        secret: `/devops/v1/projects/${projectId}/secret/${envId}/${id}`,
      };
      return axios.delete(url[type]);
    },
  }));
}
