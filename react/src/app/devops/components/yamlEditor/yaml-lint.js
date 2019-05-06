// from CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: https://codemirror.net/LICENSE
// By ale(choerodon)

(function() {
  "use strict";

  const CodeMirror = require("codemirror/lib/codemirror");
  // Depends on js-yaml.js from https://github.com/nodeca/js-yaml
  // declare global: jsyaml
  CodeMirror.registerHelper("lint", "yaml", function(text) {
    let jsyaml;
    let found = [];
    if (!jsyaml) {
      jsyaml = require("js-yaml");
    }
    try {
      jsyaml.load(text);
    } catch (e) {
      let loc = e.mark,
        // js-yaml YAMLException doesn't always provide an accurate lineno
        // e.g., when there are multiple yaml docs
        // ---
        // ---
        // foo:bar
        from = loc
          ? CodeMirror.Pos(loc.line, loc.column)
          : CodeMirror.Pos(0, 0),
        to = from;
      found.push({ from: from, to: to, message: e.message });
    }
    return found;
  });
})();
