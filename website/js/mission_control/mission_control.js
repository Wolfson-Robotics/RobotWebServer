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

    const amREQ = await fetch(window.location.protocol + "/robot/all_methods");
    const amRES = await amREQ.json();
    if (amRES.error) {
        alert("An error occurred fetching the robot methods. Details:\n" + amRES.error);
        return;
    }

    window.robotMethods = amRES;

    class TypeQualifier {

        qualifier;
        type;

        constructor(type, qualifier) {
            this.qualifier = qualifier ?? function() { return true; };
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

    missionControlLib.typeArgs = (rawStringArgs) => {

        const finalArgs = [];
        rawStringArgs.forEach((rArg, rIndex) => {

            const rLArg = rArg.toLowerCase().trim();
            if (rArg.startsWith("'") && rArg.endsWith("'")) {
                finalArgs[rIndex] = {"char": rArg.replaceAll("'", "")};
                return;
            }
            if (rArg.startsWith("\"") && rArg.endsWith("\"")) {
                finalArgs[rIndex] = {"String": rArg.replaceAll("\"", "")};
                return;
            }
            if (rLArg.endsWith("l") && missionControlLib.charCount(rLArg, "l") === 1 && !isNaN(missionControlLib.parseInteger(rLArg.split("l")[0]))) {
                finalArgs[rIndex] = {"long": missionControlLib.parseInteger(rArg)};
                return;
            }
            if (rLArg.endsWith("f") && missionControlLib.charCount(rLArg, "f") === 1 && !isNaN(Number.parseFloat(rLArg.split("f")[0]))) {
                finalArgs[rIndex] = {"float": Number.parseFloat(rArg)};
                return;
            }
            if (rLArg.endsWith("d") && missionControlLib.charCount(rLArg, "d") === 1 && !isNaN(Number.parseFloat(rLArg.split("d")[0]))) {
                finalArgs[rIndex] = {"double": Number.parseFloat(rArg)};
                return;
            }
            if (!isNaN(missionControlLib.parseByte(rArg))) {
                finalArgs[rIndex] = {"byte": rArg};
                return;
            }
            // TODO: Boolean from variable conditional operation
            if (rArg === "true" || rArg === "false") {
                finalArgs[rIndex] = {"boolean": rArg};
                return;
            }
            if (!isNaN(missionControlLib.parseInteger(rArg))) {
                finalArgs[rIndex] = {"int": missionControlLib.parseInteger(rArg)};
                return;
            }
            if (!isNaN(missionControlLib.parseDouble(rArg))) {
                finalArgs[rIndex] = {"double": Number.parseFloat(rArg)};
                return;
            }
            // TODO: Variable handling

            finalArgs[rIndex] = {"unknown": rArg};


        });
        return finalArgs;

    };


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
                const callArg = Object.values(typedCallArgs[argIndex])[0];
                const callArgType = Object.keys(typedCallArgs[argIndex])[0];

                // If the type itself is already compatible with the method arg type
                if (callArgType === supportedArgType) {
                    typedArgs[argIndex] = Object.of(supportedArgType, callArg);
                    typedScore[argIndex] = 0;
                    return;
                }
                const typeCast = missionControlLib.typeOverloads[callArgType].find(qual => qual.qualifies(callArg));
                // If an overload for the type has been found that is compatible with the method arg
                if (typeCast) {
                    const typeCastIndex = missionControlLib.typeOverloads[callArgType].indexOf(typeCast);
                    typedArgs[argIndex] = Object.of(supportedArgType, callArg);
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

    missionControlLib.callMethod = callPayload => window.callAPI("call_method", callPayload);


    const scriptsToLoad = ["code_editor.js", "code_buttons.js"];
    scriptsToLoad.forEach(script => import("/js/mission_control/" + script).then(module => module.run(missionControlLib)));
    // loadScriptAsync("/js/mission_control/code_editor.js");
    // loadScriptAsync("/js/mission_control/code_buttons.js");

})();