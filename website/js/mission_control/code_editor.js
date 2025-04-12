export function run(missionLib) {


    const complexExpressions = ["public", "private", "protected", "if", "for", "while", "else", "class"];
    function handleComplexExpression(line, found) {
        let flag = false;
        complexExpressions.forEach(ex => {
            if (line.startsWith(ex)) {
                flag = true;
                found.push({
                    from: CodeMirror.Pos(0, line.length),
                    to: CodeMirror.Pos(0, line.length),
                    message: "Coding with expression " + ex + " is not supported yet.",
                    severity: "warning"
                });
            }
        });
        return flag;
    }


    CodeMirror.registerHelper("lint", "text/x-java", (text) => {


        const found = [];
        text.replaceAll("\r", "").split("\n").forEach((line, i) => {

            line = line.trim();
            if (line === "") return;
            if (line.startsWith("//")) return;

            if (line.split(";").length > 2) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "Only one semicolon is allowed per line.",
                    severity: "error"
                });
                return;
            }
            if (!line.endsWith(";")) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "Line must end with a semicolon.",
                    severity: "error"
                });
                return;
            }
            if (handleComplexExpression(line, found)) return;


            const methodCallComps = line.split("(");
            if (methodCallComps.length > 2) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "More than one opening parenthesis found in method call.",
                    severity: "error"
                });
                return;
            }
            if (methodCallComps.length === 0) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "No opening parenthesis for method call found.",
                    severity: "error"
                });
                return;
            }

            const methodName = methodCallComps[0];
            const methodArgs = window.robotMethods[methodName];
            if (!methodArgs) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: `Unknown method ${methodName}.`,
                    severity: "error"
                });
                return;
            }

            const methodCallSpecs = methodCallComps[1].split(")");
            if (methodCallSpecs.length > 2) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "Multiple closing parentheses for method call.",
                    severity: "error"
                });
                return;
            }
            if (methodCallSpecs.length === 1) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "No closing parenthesis for method call.",
                    severity: "error"
                });
                return;
            }


            const rawMethodCallArgs = methodCallSpecs[0].split(",").map(v => v.trim());

            const methodCallArgMap = missionLib.typeArgs(rawMethodCallArgs);
            const methodCallArgs = methodCallArgMap.map(arg => Object.values(arg)[0]);
            const methodCallArgTypes = methodCallArgMap.map(arg => Object.keys(arg)[0]);

            const unknownArgs = methodCallArgMap.filter(arg => arg.unknown).map(arg => arg.unknown);
            if (unknownArgs.length > 0) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: "Argument(s) " + unknownArgs.join(", ") + " are of unknown type.",
                    severity: "error"
                });
                return;
            }

            let typedArgs = missionLib.locateMethod(methodArgs, methodCallArgMap);
            // If all goes well, then by the end of the argument parsing execution,
            // typedArgs variable will have been reset to null and will not
            // have been set to an array by argument parsing fails in the last iteration
            if (!typedArgs) {
                found.push({
                    from: CodeMirror.Pos(i, line.length),
                    to: CodeMirror.Pos(i, line.length),
                    message: `Method ${methodName} cannot be called with input types ${methodCallArgTypes.join(", ")}.`,
                    severity: "error"
                });
            }


        });

        return found;
    });


    window.editor = CodeMirror.fromTextArea(document.getElementById("code-editor"), {
        mode: "text/x-java",
        lineNumbers: true,
        theme: "default",
        indentUnit: 4,
        indentWithTabs: true,
        gutters: ["CodeMirror-linenumbers", "CodeMirror-lint-markers"],
        lint: true
    });


}