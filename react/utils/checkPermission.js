import { axios } from '@choerodon/boot';

export default async function checkPermission({ projectId, organizationId, resourceType, code, codeArr }) {
  try {
    const res = await axios.post(`iam/choerodon/v1/permissions/menus/check-permissions?projectId=${projectId}`, JSON.stringify(codeArr || [code]));

    if (res && res.failed) {
      return false;
    } else if (res && res.length) {
      if (codeArr && codeArr.length) {
        const result = [];
        codeArr.forEach((item, index) => {
          const permission = res.find(({ code: resCode }) => resCode === item);
          const { approve } = permission || {};
          result[index] = approve;
        });
        return result;
      }
      const [{ approve }] = res;
      return approve;
    }
  } catch (e) {
    // Choerodon.handleResponseError(e);
    return false;
  }
}
