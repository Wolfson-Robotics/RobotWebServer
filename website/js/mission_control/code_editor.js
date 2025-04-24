export function run(missionLib) {

    const varTypes = ["int", "double", "float", "long", "byte", "short", "boolean", "char", "String"];
    const complexExpressions = ["public", "private", "protected", "if", "for", "while", "else", "class"];

    function err(line, lineNum, message, severity = "error") {
        return {
            from: CodeMirror.Pos(lineNum, line.length),
            to: CodeMirror.Pos(lineNum, line.length),
            message: message,
            severity: severity
        };
    }

    function handleComplexExpression(line, lineNum) {
        const firstWord = line.split(" ")[0].toLowerCase().trim();
        const found = complexExpressions.find(exp => exp === firstWord);
        return found ? err(line, lineNum, `Coding with expression ${found} is not supported yet.`) : found;
    }

    function handleVar(line, lineNum) {

        const comps = line.split(" ").map(c => c.trim());
        if (comps.length === 0) return;
        const varType = varTypes.find(type => comps[0] === type);
        if (!varType) {
            return;
            /*return (comps.length >= 3 && comps[2].trim() === "=") ? {
                from: CodeMirror.Pos(0, line.length),
                to: CodeMirror.Pos(0, line.length),
                message: `Unknown type ${varType}.`,
                severity: "error"
            } : undefined;*/
        }

        if (comps.length < 4) {
            return err(line, lineNum, "Improper variable declaration.");
        }


        const name = comps[1];
        if (comps[2].trim() !== "=") {
            return err(line, lineNum, "The variable name has spaces.");
        }

        comps.shift();
        comps.shift();
        comps.shift();

        const val = comps.join(" ");
        const valVar = missionLib.typeArg(val);
        if (!missionLib.typesCompatible(valVar.type, varType)) {
            return err(line, lineNum, `Implied type of the value is incompatible with the declared type ${varType}.`)
        }

        if (missionLib.getVar(name)) {
            return err(line, lineNum, `The variable ${name} already exists.`);
        }
        missionLib.declareVar(name, valVar);
        return missionLib.getVar(name);
    }


    function parseLine(line, lineNum) {

        line = line.trim();
        if (line === "") return;
        if (line.startsWith("//")) return;

        if (line.split(";").length > 2) {
            return err(line, lineNum, "Only one semicolon is allowed per line.");
        }
        if (!line.endsWith(";")) {
            return err(line, lineNum, "Line must end with a semicolon.");
        }
        const complexErr = handleComplexExpression(line);
        if (complexErr) return complexErr;

        const varRes = handleVar(line.replaceAll(";", ""), lineNum);
        if (varRes) return varRes;


        const methodCallComps = line.split("(");
        if (methodCallComps.length > 2) {
            return err(line, lineNum, "More than one opening parenthesis found in method call.");
        }
        if (methodCallComps.length === 0) {
            return err(line, lineNum, "No opening parenthesis for method call found.");
        }

        const methodName = methodCallComps[0];
        const methodArgs = window.robotMethods[methodName];
        if (!methodArgs) {
            return err(line, lineNum, `Unknown method ${methodName}.`);
        }

        const methodCallSpecs = methodCallComps[1].split(")");
        if (methodCallSpecs.length > 2) {
            return err(line, lineNum, "Multiple closing parentheses for method call.");
        }
        if (methodCallSpecs.length === 1) {
            return err(line, lineNum, "No closing parenthesis for method call.");
        }


        const methodCallArgMap = missionLib.typeArgs(methodCallSpecs[0].split(",").map(v => v.trim()).filter(v => v !== ""));
        const methodCallArgs = methodCallArgMap.map(arg => arg.value);
        const methodCallArgTypes = methodCallArgMap.map(arg => arg.type);

        const unknownArgs = methodCallArgMap.filter(arg => arg.type === "unknown").map(arg => arg.value);
        if (unknownArgs.length > 0) {
            return err(line, lineNum, "Argument(s) " + unknownArgs.join("\", \"") + " are of unknown type.");
        }

        let foundMethod = missionLib.locateMethod(methodArgs, methodCallArgMap);
        // If all goes well, then by the end of the argument parsing execution,
        // typedArgs variable will have been reset to null and will not
        // have been set to an array by argument parsing fails in the last iteration
        if (!foundMethod) {
            return err(line, lineNum, `Method ${methodName} cannot be called with input types ${methodCallArgTypes.join(", ")}.`);
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
    window.editor.textToParsed = (text) => {
        missionLib.clearVars();
        return window.toLines(text).map((line, i) => parseLine(line, i));
    };
    // Maps each line to an array containing the line number and the line's parsed contents
    // Is represented as an array in order to programatically distinguish between
    // variables and methods
    window.editor.textToMethods = (text) => {
        missionLib.clearVars();
        return window.toLines(text).map((line, i) => [i, parseLine(line, i)]).filter(final => final[1] && final[1].args);
    };

    CodeMirror.registerHelper("lint", "text/x-java", text => {
        // Ensure previous variables from previous linting operations are erased
        missionLib.clearVars();
        return window.editor.textToParsed(text).filter(res => res && res.severity);
    });

    import("./code_actions.js").then(mod => mod.run(missionLib));


}