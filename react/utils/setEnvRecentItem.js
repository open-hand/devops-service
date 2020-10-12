export default function setRecentItem({ value: recent }) {
  if (recent) {
    const recents = localStorage.envRecentItem ? JSON.parse(localStorage.envRecentItem) : [];
    const recentItem = saveRecent(
      recents,
      recent, 10,
    );
    localStorage.envRecentItem = JSON.stringify(recentItem);
  }
}

function saveRecent(collection = [], value, number) {
  const index = findDataIndex(collection, value);
  if (index !== -1) {
    collection.splice(index, 1);
  }
  collection.unshift(value);
  return collection.slice(0, number);
}

function findDataIndex(collection, value) {
  return collection ? collection.findIndex(
    ({
      id, projectId, organizationId, code,
    }) => (id === value.id || code === value.code)
      && String(organizationId) === String(value.organizationId)
      && String(projectId) === String(value.projectId),
  ) : -1;
}
