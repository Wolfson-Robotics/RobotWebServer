export function run(missionLib) {


    const complexExpressions = ["public", "private", "protected", "if", "for", "while", "else", "class"];
    function handleComplexExpression(line) {
        let found;
        complexExpressions.forEach(ex => {
            if (line.startsWith(ex)) {
                found = {
                    from: CodeMirror.Pos(0, line.length),
                    to: CodeMirror.Pos(0, line.length),
                    message: "Coding with expression " + ex + " is not supported yet.",
                    severity: "warning"
                };
            }
        });
        return found;
    }


    function parseLine(line, lineNum) {

        line = line.trim();
        if (line === "") return;
        if (line.startsWith("//")) return;

        if (line.split(";").length > 2) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "Only one semicolon is allowed per line.",
                severity: "error"
            };
        }
        if (!line.endsWith(";")) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "Line must end with a semicolon.",
                severity: "error"
            };
        }
        const complexErr = handleComplexExpression(line);
        if (complexErr) return complexErr;


        const methodCallComps = line.split("(");
        if (methodCallComps.length > 2) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "More than one opening parenthesis found in method call.",
                severity: "error"
            };
        }
        if (methodCallComps.length === 0) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "No opening parenthesis for method call found.",
                severity: "error"
            };
        }

        const methodName = methodCallComps[0];
        const methodArgs = window.robotMethods[methodName];
        if (!methodArgs) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: `Unknown method ${methodName}.`,
                severity: "error"
            };
            return;
        }

        const methodCallSpecs = methodCallComps[1].split(")");
        if (methodCallSpecs.length > 2) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "Multiple closing parentheses for method call.",
                severity: "error"
            };
            return;
        }
        if (methodCallSpecs.length === 1) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "No closing parenthesis for method call.",
                severity: "error"
            };
            return;
        }


        const methodCallArgMap = missionLib.typeArgs(methodCallSpecs[0].split(",").map(v => v.trim()));
        const methodCallArgs = methodCallArgMap.map(arg => Object.values(arg)[0]);
        const methodCallArgTypes = methodCallArgMap.map(arg => Object.keys(arg)[0]);

        const unknownArgs = methodCallArgMap.filter(arg => arg.unknown !== undefined).map(arg => arg.unknown);
        if (unknownArgs.length > 0) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: "Argument(s) " + unknownArgs.join("\", \"") + " are of unknown type.",
                severity: "error"
            };
        }

        let foundMethod = missionLib.locateMethod(methodArgs, methodCallArgMap);
        // If all goes well, then by the end of the argument parsing execution,
        // typedArgs variable will have been reset to null and will not
        // have been set to an array by argument parsing fails in the last iteration
        if (!foundMethod) {
            return {
                from: CodeMirror.Pos(lineNum, line.length),
                to: CodeMirror.Pos(lineNum, line.length),
                message: `Method ${methodName} cannot be called with input types ${methodCallArgTypes.join(", ")}.`,
                severity: "error"
            };
        }
        return {
            name: methodName,
            args: foundMethod
        };
        
        
    }


    window.editor = CodeMirror.fromTextArea(document.getElementById("code-editor"), {
        mode: "text/x-java",
        lineNumbers: true,
        theme: "default",
        indentUnit: 4,
        indentWithTabs: true,
        gutters: ["CodeMirror-linenumbers", "CodeMirror-lint-markers"],
        lint: true
    });
    const previousCode = window.localStorage.getItem("codeEditor");
    if (previousCode) {
        window.editor.setValue(previousCode);
    }

    window.editor.on("change", () => {
        if (!window.editor.isEmpty()) {
            window.localStorage.setItem("codeEditor", window.editor.getValue());
        }
    });

    window.editor.isEmpty = function() { return this.getValue().trim() === ""};
    window.editor.textToMethods = text =>
        window.toLines(text).map((line, i) => parseLine(line, i));
    CodeMirror.registerHelper("lint", "text/x-java", text =>
        window.editor.textToMethods(text).filter(res => res && res.severity)
    );

    import("./code_actions.js").then(mod => mod.run(missionLib));


}