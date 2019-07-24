const QUALITY_LIST = [
  {
    key: "bugs",
    icon: "bug_report",
    hasReport: true,
  },
  {
    key: "vulnerabilities",
    icon: "unlock",
    hasReport: true,
  },
  {
    icon: "bug_report",
    key: "new_bugs",
    hasReport: false,
  },
  {
    icon: "unlock",
    hasReport: false,
    key: "new_vulnerabilities",
  },
  {
    icon: "opacity",
    key: "sqale_index",
    hasReport: false,
  },
  {
    icon: "group_work",
    key: "code_smells",
    hasReport: true,
  },
  {
    icon: "opacity",
    key: "new_technical_debt",
    hasReport: false,
  },
  {
    icon: "group_work",
    key: "new_code_smells",
    hasReport: false,
  },
  {
    icon: "fiber_smart_record",
    key: "coverage",
    hasReport: true,
    isPercent: true,
  },
  {
    icon: "hdr_strong",
    key: "tests",
    hasReport: false,
  },
  {
    icon: "fiber_smart_record",
    key: "new_coverage",
    hasReport: false,
    isPercent: true,
  },
  {
    icon: "adjust",
    key: "duplicated_lines_density",
    hasReport: true,
    isPercent: true,
  },
  {
    icon: "adjust",
    key: "duplicated_blocks",
    hasReport: false,
  },
  {
    icon: "adjust",
    key: "new_duplicated_lines_density",
    hasReport: false,
    isPercent: true,
  },
];

const OBJECT_TYPE = {
  reliability: "issue",
  maintainability: "issue",
  coverage: "coverage",
  duplications: "duplicate",
};

export {
  QUALITY_LIST,
  OBJECT_TYPE,
};
