/* eslint-disable import/no-anonymous-default-export */
/* eslint-disable no-param-reassign */
import JSONbig from 'json-bigint';

export default ({ organizationId, projectId }) => ({
  autoCreate: false,
  autoQuery: true,
  paging: false,
  transport: {
    read: {
      url: `/rdupm/v1/nexus-repositorys/${organizationId}/project/${projectId}/ci/repo/list?repoType=MAVEN&type=hosted`,
      method: 'get',
      transformResponse: (res) => {
        let newRes = JSON.parse(res);
        try {
          newRes = newRes.map((item) => {
            item.repositoryId = JSONbig.parse(item.repositoryId);
            return item;
          });
          return newRes;
        } catch (e) {
          throw new Error(e);
        }
      },
    },
  },
});
