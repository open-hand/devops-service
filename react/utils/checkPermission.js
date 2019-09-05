import { axios } from '@choerodon/master';

export default async function checkPermission({ projectId, organizationId, resourceType, code }) {
  try {
    const res = await axios.post('/base/v1/permissions/checkPermission', JSON.stringify([{
      code,
      organizationId,
      projectId,
      resourceType,
    }]));

    if (res && res.failed) {
      return false;
    } else if (res && res.length) {
      const [{ approve }] = res;
      return approve;
    }
  } catch (e) {
    Choerodon.handleResponseError(e);
    return false;
  }
}
