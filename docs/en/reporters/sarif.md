# SARIF reporter

Reporter option - `sarif`

## Description

Output the analysis results into file bsl-ls.sarif in the current working directory. OASIS file format is specified and available here: [https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html](https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html).

## Sample output

```json
{
  "$schema" : "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
  "version" : "2.1.0",
  "runs" : [ {
    "tool" : {
      "driver" : {
        "name" : "BSL Language Server",
        "organization" : "1c-syntax",
        "version" : "0.19.0",
        "informationUri" : "https://1c-syntax.github.io/bsl-language-server",
        "rules" : [ {
          "id" : "MissingSpace",
          "name" : "Missing spaces to the left or right of operators `+ - * / = % &lt; &gt; &lt;&gt; &lt;= &gt;=`, keywords, and also to the right of `,` and `;`",
          "fullDescription" : {
            "text" : "# Missing spaces to the left or right of operators `+ - * / = % &lt; &gt; &lt;&gt; &lt;= &gt;=`, keywords, and also to the right of `,` and `;`",
            "markdown" : "# Missing spaces to the left or right of operators `+ - * / = % &lt; &gt; &lt;&gt; &lt;= &gt;=`, keywords, and also to the right of `,` and `;`"
          },
          "defaultConfiguration" : {
            "level" : "none",
            "parameters" : {
              "listForCheckLeftAndRight" : "+ - * / = % < > <> <= >=",
              "allowMultipleCommas" : false,
              "checkSpaceToRightOfUnary" : false,
              "listForCheckLeft" : "",
              "listForCheckRight" : ", ;"
            }
          },
          "helpUri" : "https://1c-syntax.github.io/bsl-language-server/diagnostics/MissingSpace",
          "properties" : {
            "tags" : [ "BADPRACTICE" ]
          }
        } ],
        "language" : "en"
      }
    },
    "invocations" : [ {
      "ruleConfigurationOverrides" : [ {
        "configuration" : {
          "enabled" : false
        },
        "descriptor" : {
          "id" : "Typo"
        }
      }, {
        "configuration" : {
          "parameters" : {
            "test" : 1
          }
        },
        "descriptor" : {
          "id" : "some"
        }
      }, {
        "configuration" : { },
        "descriptor" : {
          "id" : "test"
        }
      } ],
      "executionSuccessful" : true,
      "processId" : 14596,
      "workingDirectory" : {
        "uri" : "file:///D:/git/1c-syntax/bsl-language-server/"
      }
    } ],
    "language" : "ru",
    "results" : [ {
      "ruleId" : "test",
      "level" : "error",
      "message" : {
        "text" : "message"
      },
      "analysisTarget" : {
        "uri" : "file:///D:/fake-uri.bsl"
      },
      "locations" : [ {
        "physicalLocation" : {
          "artifactLocation" : {
            "uri" : "file:///D:/fake-uri.bsl"
          },
          "region" : {
            "startLine" : 1,
            "startColumn" : 2,
            "endLine" : 3,
            "endColumn" : 4
          }
        },
        "message" : {
          "text" : "message"
        }
      } ],
      "relatedLocations" : [ ]
    } ],
    "defaultEncoding" : "UTF-8",
    "defaultSourceLanguage" : "BSL"
  } ]
}
```