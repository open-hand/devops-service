/* CodeMirror, copyright (c) by Marijn Haverbeke and others
 * Distributed under an MIT license: https://codemirror.net/LICENSE
 * 扩展yaml编辑器规则
 * By ale(choerodon)
 */

(function() {
  "use strict";
  var CodeMirror = require("codemirror/lib/codemirror")
  CodeMirror.defineMode("yaml", function() {
    var cons = ["true", "false", "on", "off", "yes", "no"];
    var keywordRegex = new RegExp("\\b((" + cons.join(")|(") + "))$", "i");
    var boolRegex = new RegExp(
      "\\b(?:true|false|TRUE|FALSE|True|False|yes|no)\\b"
    );

    return {
      token: function(stream, state) {
        var ch = stream.peek();
        var esc = state.escaped;
        state.escaped = false;
        /* comments */
        if (ch == "#" && (stream.pos == 0 || /\s/.test(stream.string.charAt(stream.pos - 1)))) {
          stream.skipToEnd();
          return "comment";
        }

        if (stream.match(/^('([^']|\\.)*'?|"([^"]|\\.)*"?)/)) return "string";

        if (state.literal && stream.indentation() > state.keyCol) {
          stream.skipToEnd();
          return "string";
        } else if (state.literal) {
          state.literal = false;
        }
        if (stream.sol()) {
          state.keyCol = 0;
          state.pair = false;
          state.pairStart = false;
          /* document start */
          if (stream.match(/---/)) {
            return "def";
          }
          /* document end */
          if (stream.match(/\.\.\./)) {
            return "def";
          }
          /* array list item */
          if (stream.match(/\s*-\s+/)) {
            return "meta";
          }
        }
        /* inline pairs/lists */
        if (stream.match(/^(\{|\}|\[|\])/)) {
          if (ch == "{") state.inlinePairs++;
          else if (ch == "}") state.inlinePairs--;
          else if (ch == "[") state.inlineList++;
          else state.inlineList--;
          return "meta";
        }

        /* list seperator */
        if (state.inlineList > 0 && !esc && ch == ",") {
          stream.next();
          return "meta";
        }
        /* pairs seperator */
        if (state.inlinePairs > 0 && !esc && ch == ",") {
          state.keyCol = 0;
          state.pair = false;
          state.pairStart = false;
          stream.next();
          return "meta";
        }

        if (stream.match(/(\w+?)(\s*:(?=\s|$))/)) {
          return "atom";
        }

        if (
          (state.inlineList > 0 || state.inlinePairs > 0) &&
          stream.match(
            /(\b|[+\-\.])[\d_]+(?:(?:\.[\d_]*)?(?:[eE][+\-]?[\d_]+)?)(?=[^\d-\w]|$)/
          )
        ) {
          return "number";
        }

        if (
          (state.inlineList > 0 || state.inlinePairs > 0) &&
          stream.match(boolRegex)
        ) {
          return "boolean";
        }

        /* references */
        if (stream.match(/^\s*(\&|\*)[a-z0-9\._-]+\b/i)) {
          return "variable-2";
        }

        /* start of value of a pair */
        if (state.pairStart) {
          /* block literals */
          if (stream.match(/^\s*(\||\>)\s*/)) {
            state.literal = true;
            return "meta";
          }

          /* float */
          if (
            stream.match(
              /(\b|[+\-\.])[\d_]+(?:(?:\.[\d_]*)?(?:[eE][+\-]?[\d_]+)?)(?=[^\d-\w]|$)/
            )
          ) {
            return "number";
          }
          /* other number */
          if (
            state.inlinePairs == 0 &&
            stream.match(/[+\-]?\.inf\b|NaN\b|0x[\dA-Fa-f_]+|0b[10_]+/)
          ) {
            return "number";
          }

          /* keywords */
          if (stream.match(keywordRegex)) {
            return "keyword";
          }
          /*boolean*/
          if (stream.match(boolRegex)) {
            return "boolean";
          }
        }

        /* pairs (associative arrays) -> key */
        if (
          !state.pair &&
          stream.match(
            /^\s*(?:[,\[\]{}&*!|>'"%@`][^\s'":]|[^,\[\]{}#&*!|>'"%@`])[^#]*?(?=\s*:($|\s))/
          )
        ) {
          state.pair = true;
          state.keyCol = stream.indentation();
          return "atom";
        }
        if (state.pair && stream.match(/^:\s*/)) {
          state.pairStart = true;
          return "meta";
        }
        /* number list */
        if (
          !state.pair &&
          stream.match(
            /(\b|[+\-\.])[\d_]+(?:(?:\.[\d_]*)?(?:[eE][+\-]?[\d_]+)?)(?=[^\d-\w]|$)/
          )
        ) {
          state.pair = true;
          state.keyCol = stream.indentation();
          return "number";
        }

        /* boolean list */
        if (!state.pair && stream.match(boolRegex)) {
          state.pair = true;
          state.keyCol = stream.indentation();
          return "boolean";
        }

        /* nothing found, continue */
        state.pairStart = false;
        state.escaped = ch == "\\";
        stream.next();
        return null;
      },
      startState: function() {
        return {
          pair: false,
          pairStart: false,
          keyCol: 0,
          inlinePairs: 0,
          inlineList: 0,
          literal: false,
          escaped: false,
        };
      },
      lineComment: "#",
      fold: "indent",
    };
  });

  CodeMirror.defineMIME("text/chd-yaml", "yaml");
})();
