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
	
	const afRES = await window.getAPI(window.getEndpoint("allFields")).then(res => res.json());
	if (afRES.error) {
		alert("An error occurred fetching the robot fields. Details:\n" + afRES.error);
		return;
	}
	window.robotFields = afRES;


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
        "String": TypeQualifier.of("Object"),
        "byte": TypeQualifier.of("double", "float", "long", "short"),
        "short": TypeQualifier.of("double", "float", "long"),
        "char": TypeQualifier.of("int", "double", "float", "long"),
		"com.qualcomm.robotcore.hardware.DcMotorEx": TypeQualifier.of("com.qualcomm.robotcore.hardware.DcMotor")
    }
	// Insert field types as overloads
	Object.values(window.robotFields).forEach(f => {
		if (missionControlLib.typeOverloads[f[0]]) return;
		// Copy array as to not mutate the original
		let fDescendants = f;
		fDescendants.shift();
		missionControlLib.typeOverloads[f[0]] = TypeQualifier.of(...fDescendants);
	});


    missionControlLib.typeArg = (rArg) => {
        rArg = rArg.trim();

        // Exclusively use rLArg to check for number modifiers irrespectivve of capitalization
        const rLArg = rArg.toLowerCase().trim();
        if (rArg.startsWith("'") && rArg.endsWith("'")) {
            return new TypeAssoc("char", rArg.replaceAll("'", ""));
        }
        if (rArg.startsWith("\"") && rArg.endsWith("\"")) {
            return new TypeAssoc("String", rArg.replaceAll("\"", ""));
        }
        // isByte and parseByte are separated so that, if parseByte fails, we know that the error
        // occurs because the byte is too small or big, and we can stop the function here as a result
        // (For instance, if we have the byte 0xff, under a simple parseByt echeck, it would proceed
        // to the float check and incorrectly pass as a float).
        if (missionControlLib.isByte(rArg)) {
            return new TypeAssoc(
                !isNaN(missionControlLib.parseByte(rArg)) ? "byte" : "unknown",
                rArg
            );
        }
        if (rLArg.endsWith("l") && missionControlLib.charCount(rLArg, "l") === 1 && !isNaN(missionControlLib.parseInteger(rLArg.split("l")[0]))) {
            return new TypeAssoc("long", missionControlLib.parseInteger(rLArg.replaceAll("l", "")));
        }
        if (rLArg.endsWith("f") && missionControlLib.charCount(rLArg, "f") === 1 && !isNaN(Number.parseFloat(rLArg.split("f")[0]))) {
            return new TypeAssoc("float", Number.parseFloat(rArg));
        }
        if (rLArg.endsWith("d") && missionControlLib.charCount(rLArg, "d") === 1 && !isNaN(Number.parseFloat(rLArg.split("d")[0]))) {
            return new TypeAssoc("double", Number.parseFloat(rArg));
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
		const realVarName = rArg.replaceAll("!", "").trim();
		// Variables and fields are separate, as variables encapsulate TypeAssocs whereas fields merely encapsulate types
        const foundVar = missionControlLib.getVar(realVarName);
        if (foundVar) {
            return new TypeAssoc(foundVar.type, foundVar.type === "boolean" ? (invCount % 2 === 0 ? foundVar.value : !foundVar.value) : foundVar.value);
        }
		const foundField = window.robotFields[realVarName];
		if (foundField) {
			// Associate the type with the field name so that it may retrieved on the server-side
			return new TypeAssoc(foundField[0], realVarName);
		}
        return new TypeAssoc("unknown", rArg);
    }



    missionControlLib.typeArgs = (rawStringArgs) => rawStringArgs.map(rArg => missionControlLib.typeArg(rArg));

    missionControlLib.typesCompatible = (arg, type, castedType) => type === castedType || missionControlLib.typeOverloads[type].find(qual => qual.type === castedType && qual.qualifies(arg));


    // Don't declare variables as a member of missionControlLib for encapsulation
    let variables = [];
    missionControlLib.declareVar = (name, type, value) => {
        if (type instanceof TypeAssoc) {
            missionControlLib.declareVar(name, type.type, type.value);
            return;
        }
        variables.push(new Variable(name, type, value));
    };
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
				if (!typedArgs || !typedScore) {
					return;
				}
                const callArg = typedCallArgs[argIndex].value;
                const callArgType = typedCallArgs[argIndex].type;

                // If the type itself is already compatible with the method arg type
                if (callArgType === supportedArgType) {
                    typedArgs[argIndex] = new TypeAssoc(supportedArgType, callArg);
                    typedScore[argIndex] = 0;
                    return;
                }
                const typeCast = missionControlLib.typesCompatible(callArg, callArgType, supportedArgType);
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
    // See typeArg for why these two operations are separate
    missionControlLib.isByte = (input) => {
        input = input.trim();
        return /^[+-]?0[xX][0-9A-Fa-f]{1,2}$/.test(input);
    }
    missionControlLib.parseByte = (input) => {
        input = input.trim();
        if (!missionControlLib.isByte(input)) {
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