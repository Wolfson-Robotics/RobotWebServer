(async() => {

    async function loadScript(url) {
        const load = new Promise(() => {});
        const script = document.createElement("script");
        script.onload = () => Promise.resolve(load);
        script.src = url;
        script.type = "text/javascript";
        document.head.appendChild(script);
        await load;
    }
    function loadScriptAsync(url) {
        const script = document.createElement("script");
        script.src = url;
        script.type = "text/javascript";
        document.head.appendChild(script);
    }

    const amRES = await window.getAPI(window.getEndpoint("allMethods")).then(res => res.json());
    if (amRES.error) {
        alert("An error occurred fetching the robot methods. Details:\n" + amRES.error);
        return;
    }
    window.robotMethods = amRES;

    class TypeQualifier {

        qualifier;
        type;

        constructor(type, qualifier) {
            this.qualifier = qualifier ?? function() { return true };
            this.type = type;
        }

        qualifies(input) {
            // Qualifier assumes that the appropriate input is being passed in
            return this.qualifier(input);
        }

        static of(...types) {
            return types.map(type => new TypeQualifier(type));
        }

    }

    class TypeAssoc {
        type;
        value;
        constructor(type, value) {
            this.type = type;
            this.value = value;
        }

        asReq() {
            return {
                [type]: value
            }
        }
    }

    class Variable {

        name;
        type;
        value;

        constructor(name, type, value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

    }




    const missionControlLib = {};
    // Maps raw types to what they are allowed to overload to in a method
    missionControlLib.typeOverloads = {
        "int": [
            ...TypeQualifier.of("double", "float", "long", "byte"),
            new TypeQualifier("short", num => Number(num) >= -32768 && Number(num) <= 32767)],
        "double": TypeQualifier.of("float"),
        "float": [],
        "long": [],
        "boolean": [],
        "String": [],
        "byte": TypeQualifier.of("double", "float", "long", "short"),
        "short": TypeQualifier.of("double", "float", "long"),
        "char": TypeQualifier.of("int", "double", "float", "long")
    }


    missionControlLib.typeArg = (rArg) => {
        rArg = rArg.toLowerCase().trim();

        if (rArg.startsWith("'") && rArg.endsWith("'")) {
            return new TypeAssoc("char", rArg.replaceAll("'", ""));
        }
        if (rArg.startsWith("\"") && rArg.endsWith("\"")) {
            return new TypeAssoc("String", rArg.replaceAll("\"", ""));
        }
        if (rArg.endsWith("l") && missionControlLib.charCount(rArg, "l") === 1 && !isNaN(missionControlLib.parseInteger(rArg.split("l")[0]))) {
            return new TypeAssoc("long", missionControlLib.parseInteger(rArg));
        }
        if (rArg.endsWith("f") && missionControlLib.charCount(rArg, "f") === 1 && !isNaN(Number.parseFloat(rArg.split("f")[0]))) {
            return new TypeAssoc("float", Number.parseFloat(rArg));
        }
        if (rArg.endsWith("d") && missionControlLib.charCount(rArg, "d") === 1 && !isNaN(Number.parseFloat(rArg.split("d")[0]))) {
            return new TypeAssoc("double", Number.parseFloat(rArg));
        }
        if (!isNaN(missionControlLib.parseByte(rArg))) {
            return new TypeAssoc("byte", rArg);
        }
        if (rArg === "true" || rArg === "false") {
            return new TypeAssoc("boolean", rArg === "true");
        }
        if (!isNaN(missionControlLib.parseInteger(rArg))) {
            return new TypeAssoc("int", missionControlLib.parseInteger(rArg));
        }
        if (!isNaN(missionControlLib.parseDouble(rArg))) {
            return new TypeAssoc("double", Number.parseFloat(rArg));
        }

        // Account for inversions of variable expressions
        const invCount = rArg.count("!");
        const foundVar = missionControlLib.getVar(rArg.replaceAll("!", ""));
        if (foundVar) {
            return new TypeAssoc(foundVar.type, foundVar.type === "boolean" ? (invCount % 2 === 0 ? foundVar.value : !foundVar.value) : foundVar.value);
        }
        return new TypeAssoc("unknown", rArg);
    }



    missionControlLib.typeArgs = (rawStringArgs) => rawStringArgs.map(rArg => missionControlLib.typeArg(rArg));

    missionControlLib.typesCompatible = (type, castedType) => type === castedType || missionControlLib.typeOverloads[type].find(qual => qual.type === castedType);


    // Don't declare variables as a member of missionControlLib for encapsulation
    let variables = [];
    missionControlLib.declareVar = (name, type, value) => {
        if (type instanceof TypeAssoc) {
            missionControlLib.declareVar(name, type.type, type.value);
            return;
        }
        variables.push(new Variable(name, type, value));
    }
    missionControlLib.getVar = (name) => variables.find(variable => variable.name === name);
    missionControlLib.clearVars = () => variables = [];


    missionControlLib.locateMethod = (methodArgs, typedCallArgs) => {

        let foundTypedArgs = [];
        /*
         This tracks the traversal levels required to "loosen" the given type
         to a found type in a method. So, if the given arg is an "int" and two
         single-arg methods are found with double and float, then their scores
         will be 1 and 2, respectively, since "float" is next on the associated
         typeOverloads array than "double". This is done to find the most preferred
         method to call, which the method with the lowest score will. So, in this
         case, the first method with "double" will be called. This corresponds with
         Java's system to "prefer" certain methods over others. It is also stored as
         an array to find each argument's score over the other numerically corresponding
         to prefer first arguments over later arguments.
         */
        let foundTypedScores = [];

        methodArgs.forEach(supportedArgs => {
            if (foundTypedArgs.length !== 0 && foundTypedArgs[0].length === 0) {
                return;
            }
            if (supportedArgs.length !== typedCallArgs.length) {
                return;
            }
            let typedArgs = [];
            let typedScore = [];

            if (typedCallArgs.length === 0) {
                foundTypedArgs.push([]);
                foundTypedScores.push([]);
                return;
            }
            supportedArgs.forEach(((supportedArgType, argIndex) => {
                const callArg = typedCallArgs[argIndex].value;
                const callArgType = typedCallArgs[argIndex].type;

                // If the type itself is already compatible with the method arg type
                if (callArgType === supportedArgType) {
                    typedArgs[argIndex] = new TypeAssoc(supportedArgType, callArg);
                    typedScore[argIndex] = 0;
                    return;
                }
                const typeCast = missionControlLib.typesCompatible(callArgType, supportedArgType);
                // If an overload for the type has been found that is compatible with the method arg
                if (typeCast) {
                    const typeCastIndex = missionControlLib.typeOverloads[callArgType].indexOf(typeCast);
                    typedArgs[argIndex] = new TypeAssoc(supportedArgType, callArg);
                    typedScore[argIndex] = typeCastIndex;
                    return;
                }
                typedArgs = null;
                typedScore = null;
            }));

            if (typedArgs && typedScore) {
                foundTypedArgs.push(typedArgs);
                foundTypedScores.push(typedScore);
            }
        });

        if (foundTypedArgs.length === 0) {
            return null;
        }
        // Note that all the found methods are guaranteed to have the same length at this point
        let winner = [];
        for (let i = 0; i < foundTypedArgs[0].length; i++) {
            if (winner.length === 1) {
                break;
            }
            const iScore = (winner.length === 0 ? foundTypedScores : foundTypedScores.filter(i => winner.includes(i)))
                .map(typedArgs => typedArgs[i]);
            const lowest = Math.min(...iScore);

            iScore.forEach((score, index) => {
                if (score === lowest) {
                    winner.push(index);
                } else {
                    winner.remove(index);
                }
            });
        }
        return foundTypedArgs[winner.length === 0 ? 0 : winner[0]];
    };

    // Util functions to deal with types
    missionControlLib.exponentRegex = /^[+-]?\d+([eE][+-]?\d+)$/;
    missionControlLib.charCount = (input, char) => {
        return input.split(char).length - 1;
    }


    // Number.parseInt parses doubles as well, which is not we want. This
    // function ONLY parses INTEGERS. Same goes for the following
    // functions.
    missionControlLib.parseInteger = (input) => {
        input = input.trim();
        if (/^[+-]?\d+$/.test(input) || missionControlLib.exponentRegex.test(input)) {
            const num = Number(input);
            return Number.isInteger(num) ? num : NaN;
        }
        return NaN;
    }

    // Only accepts bytes of format 0xff
    missionControlLib.parseByte = (input) => {
        input = input.trim();
        if (!/^[+-]?0[xX][0-9A-Fa-f]{1,2}$/.test(input)) {
            return NaN;
        }
        const num = Number(input);
        return num < -127 || num > 128 ? NaN : num;
    }

    missionControlLib.parseDouble = (input) => {
        input = input.trim();
        if (/^[+-]?\d+$/.test(input)) {
            return NaN;
        }

        const decimalRegex = /^[+-]?((\d+\.\d*)|(\d*\.\d+))([eE][+-]?\d+)?$/;
        if (!(decimalRegex.test(input) || missionControlLib.exponentRegex.test(input))) {
            return NaN;
        }

        const num = Number(input);
        return isNaN(num) ? NaN : num;
    }

    missionControlLib.callMethod = callPayload => window.callAPI(window.getEndpoint("callMethod"), callPayload);


    window.config.missionControl.modules.forEach(js => import(`/js/mission_control/${js}.js`).then(module => module.run(missionControlLib)));
    // loadScriptAsync("/js/mission_control/code_editor.js");
    // loadScriptAsync("/js/mission_control/code_buttons.js");

})();