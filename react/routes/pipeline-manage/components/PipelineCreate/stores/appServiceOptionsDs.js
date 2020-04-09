export default (projectId) => ({
  autoQuery: true,
  // transport: {
  //   read: {
  //     method: 'post',
  //     url: `/devops/v1/projects/${projectId}/app_service/list_app_services_without_ci`,
  //   },
  // },
  lookupAxiosConfig: () => ({
    method: 'post',
    url: `/devops/v1/projects/${projectId}/app_service/list_app_services_without_ci`,
    // transformResponse(data) {
    //   let parseData;
    //   const typeOf = Object.prototype.toString;
    //   if (typeOf.call(data) === '[object String]') {
    //     parseData = JSON.parse(data);
    //   } else {
    //     parseData = data;
    //   }
    //   if (!record) {
    //     const initSelected = parseData.map(p => p.id).splice(0, 3);
    //     dataSet.loadData([{ proSelect: initSelected }]);
    //     ProDeployStore.initData(orgId, initSelected);
    //   } else {
    //     const chosenArray = record.data.proSelect;
    //     const allProjects = ProDeployStore.getProjectsArray;
    //     parseData = [
    //       ...parseData,
    //       ...allProjects.filter(a => chosenArray.includes(a.id)),
    //     ];
    //   }
    //   ProDeployStore.setProjectArray(parseData);
    //   return parseData;
    // },
  }),
});
