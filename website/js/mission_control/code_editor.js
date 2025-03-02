(async() => {


    const numberTypes = ["double", "float", "int", "long"];
    const exponentRegex =  /^[+-]?\d+([eE][+-]?\d+)$/;
    // Util functions

    function charCount(input, char) {
        return input.split(char).length - 1;
    }
    
    // Number.parseInt parses doubles as well, which is not we want. This
    // function ONLY parses INTEGERS. Same goes for the following
    // functions.
    function parseInteger(input) {
        input = input.trim();
        if (/^[+-]?\d+$/.test(input) || exponentRegex.test(input)) {
            const num = Number(input);
            return Number.isInteger(num) ? num : NaN;
        }
        return NaN;
    }
    // Only accepts bytes of format 0xff
    function parseByte(input) {
        input = input.trim();
        if (!/^[+-]?0[xX][0-9A-Fa-f]{1,2}$/.test(input)) {
            return NaN;
        }
        const num = Number(input);
        return num < -127 || num > 128 ? NaN : num;
    }

    function parseDouble(input) {
        input = input.trim();
        if (/^[+-]?\d+$/.test(input)) {
            return NaN;
        }

        const decimalRegex = /^[+-]?((\d+\.\d*)|(\d*\.\d+))([eE][+-]?\d+)?$/;
        if (!(decimalRegex.test(input) || exponentRegex.test(input))) {
            return NaN;
        }

        const num = Number(input);
        return isNaN(num) ? NaN : num;
    }

    
    

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


    function typeArgs(rawStringArgs) {
        const finalArgs = [];
        rawStringArgs.forEach((rArg, rIndex) => {

            const rLArg = rArg.toLowerCase();
            if (rArg.startsWith("'") && rArgs.endsWith("'")) {
                finalArgs[rIndex] = {"char": arg.replaceAll("'", "")};
                return;
            }
            if (rArg.startsWith("\"") && rArg.endsWith("\"")) {
                finalArgs[rIndex] = {"String": arg.replaceAll("\"", "")};
                return;
            }
            if (rLArg.endsWith("l") && charCount(rLArg, "l") === 1 && !isNaN(parseInteger(rLArg.split("l")[0]))) {
                finalArgs[rIndex] = {"long": parseInteger(rArg)};
                return;
            }
            if (rLArg.endsWith("f") && charCount(rLArg, "f") === 1 && !isNaN(Number.parseFloat(rLArg.split("f")[0]))) {
                finalArgs[rIndex] = {"float": Number.parseFloat(rArg)};
                return;
            }
            if (rLArg.endsWith("d") && charCount(rLArg, "d") === 1 && !isNaN(Number.parseFloat(rLArg.split("d")[0]))) {
                finalArgs[rIndex] = {"double": Number.parseFloat(rArg)};
                return;
            }
            if (!isNaN(parseByte(rArg))) {
                finalArgs[rIndex] = {"byte": rArg};
                return;
            }
            // TODO: Boolean from variable conditional operation
            if (rArg === "true" || rArg === "false") {
                finalArgs[rIndex] = {"boolean": rArg};
                return;
            }
            if (!isNaN(parseInteger(rArg))) {
                finalArgs[rIndex] = {"int": parseInteger(rArg)};
                return;
            }
            if (!isNaN(parseDouble(rArg))) {
                finalArgs[rIndex] = {"double": Number.parseFloat(rArg)};
                return;
            }
            // TODO: Variable handling

            finalArgs[rIndex] = {"unknown": rArg};


        });
        return finalArgs;
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
                message: "Unknown method.",
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

        // TODO: Methods which take no arguments

        const rawMethodCallArgs = methodCallSpecs[0].split(",").map(v => v.trim());
        const methodCallArgMap = typeArgs(rawMethodCallArgs);
        const methodCallArgs = methodCallArgMap.map(arg => Object.values(arg)[0]);
        const methodCallArgTypes = methodCallArgMap.map(arg => Object.keys(arg)[0]);
        // TODO: Variable handling

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

        let typingFail = false;
        let typedArgs = [];
        methodArgs.forEach(supportedArgs => {
            if (supportedArgs.length !== methodCallArgs.length) {
                typingFail = true;
                return;
            }
            if (typedArgs.length > 0) {
                return;
            }
            typingFail = false;

            // TODO: Deal with "preferred" method overloads (that is,
            // prefer a method that takes in an int and a String rather than
            // a short and a String)
            supportedArgs.forEach(((supportedArgType, argIndex) => {

                const callArg = methodCallArgs[argIndex];
                const callArgType = methodCallArgTypes[argIndex];

                // Go through specific cases where other *apparent* types
                // are allowed to be passed in. For instance, if a method
                // only accepts double, but the *apparent* type is an int,
                // then accept the input.
                let noCheck = false;
                switch (supportedArgType) {
                    case "short":
                        if (callArgType === supportedArgType) break;
                        if (callArgType !== "int" || callArg < -32768 || callArg > 32767) {
                            typingFail = true;
                            return;
                        }
                        noCheck = true;
                        break;
                    case "double":
                        if (callArgType === supportedArgType) break;
                        if (!numberTypes.includes(callArgType) || callArgType === "float" || callArgType === "long") {
                            typingFail = true;
                            return;
                        }
                        noCheck = true;
                        break;
                    case "float":
                        if (callArgType === supportedArgType) break;
                        if (!numberTypes.includes(callArgType) || callArgType === "long") {
                            typingFail = true;
                            return;
                        }
                        // If it is a double with more than seven digits, then it cannot be a float
                        if (callArgType === "double" && rawCallMethodArgs[argIndex].split(".")[1].length > 7) {
                            typingFail = true;
                            return;
                        }
                        noCheck = true;
                        break;
                    case "byte":
                        if (callArgType === supportedArgType) break;
                        // This handles the case when the byte passed in is an int,
                        // since that is allowed. The case when the byte is a hexadecimal
                        // is quietly deferred to the check succeeding the switch loop,
                        // since in that case the returned callArgType would be "byte".
                        if (callArgType === "int" && (callArg < -128 || callArg > 127)) {
                            typingFail = true;
                            return;
                        }
                        noCheck = true;
                        break;
                }
                if (callArgType !== supportedArgType && !noCheck) {
                    typingFail = true;
                    return;
                }
                typedArgs[argIndex] = {supportedArgType: callArg};

            }));
        });

        // If all goes well, then by the end of the argument parsing execution,
        // the typingFail variable will have been reset to false and will not
        // have been set to true by argument parsing fails in the last iteration
        if (typingFail) {
            found.push({
                from: CodeMirror.Pos(0, 0),
                to: CodeMirror.Pos(0, 0),
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



})();