import { axios } from '@choerodon/boot';

export default async function checkPermission({ projectId, organizationId, resourceType, code }) {
  try {
    const res = await axios.post(`iam/hzero/v1/menus/check-permissions?projectId=${projectId}`, JSON.stringify([code]));

    if (res && res.failed) {
      return false;
    } else if (res && res.length) {
      const [{ approve }] = res;
      return approve;
    }
  } catch (e) {
    // Choerodon.handleResponseError(e);
    return false;
  }
}
