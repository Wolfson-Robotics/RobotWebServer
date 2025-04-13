export function run(missionLib) {

    function submit(event, methodName, form) {
        event.preventDefault();

        const typedArgs = missionLib.typeArgs(Array.from(form.elements)
            .filter(i => i.type !== "submit")
            .map(input => {

                switch (input.type) {
                    case "checkbox":
                        return input.checked ? "true" : "false";
                    default:
                        return input.wrapper + input.value + input.wrapper;
                }

            })
        );
        const methodTyped = missionLib.locateMethod(window.robotMethods[methodName], typedArgs);


        const callPayload = {
            name: methodName,
            args: typedArgs.keyMap((k, i) => methodTyped[i])
        };
        console.log(callPayload);

        missionLib.callMethod(callPayload).catch(err => {
            window.alert("An error occurred when calling method:\n" + err);
            console.log(err);
        });
    }

    function dataTypeToInputType(dataType) {
        const domInput = document.createElement("input");
        domInput.required = true;
        domInput.wrapper =  "";
        switch (dataType) {
            case "float":
            case "double":
                domInput.type = "number";
                domInput.step = "any";
                break;
            case "int":
            case "long":
            case "short":
                domInput.type = "number";
                break;
            case "boolean":
                domInput.type = "checkbox";
                break;
            case "char":
                domInput.type = "text";
                domInput.wrapper = "'";
                domInput.addEventListener("input", e => {
                    if (domInput.length >= 1) e.preventDefault();
                });
                break;
            case "String":
                domInput.type = "text";
                domInput.wrapper = '"';
                break;
            default:
                domInput.type = "text";
                break;
        }
        return domInput;
    }


    console.log("Processing inputs...");

    const domInputs = document.getElementById("inputs");
    Object.entries(window.robotMethods).forEach(([methodName, args]) => {

        const domForm = document.createElement("form");
        domForm.id = methodName;
        domForm.addEventListener("submit", e => submit(e, methodName, domForm));

        if (args.length !== 0) {
            args[0].forEach(input => domForm.appendChild(dataTypeToInputType(input)));
        }

        const domButton = document.createElement("input");
        domButton.type = "submit";
        domButton.value = methodName;
        domForm.appendChild(domButton);

        domInputs.appendChild(domForm);

    });

}