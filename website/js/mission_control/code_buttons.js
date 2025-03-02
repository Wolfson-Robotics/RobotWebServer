function submit(event, methodName, form) {
    event.preventDefault();

    const json = {};
    json.name = methodName;

    const formData = new FormData(form);
    /*
    formData.forEach((value, key) => {
        args.push(value);
    });*/
    // formData.forEach(args.push);

    json["args"] = [];
    Array.from(form.elements).forEach(input => {

        switch (input.type) {
            case "submit":
                return;
            case "double":
            case "number":
                json["args"].push(Number(input.value));
                return;
            case "checkbox":
                json["args"].push(input.checked);
                return;
            default:
                json["args"].push(input.value);
                return;
        }

    });

    console.log(JSON.stringify(json));

    fetch(window.location.protocol + '/robot/call_method', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(json),
    });
}

function dataTypeToInputType(dataType) {
    var domInput = document.createElement("input");
    domInput.required = true;
    switch (dataType) {
        case "double":
            domInput.type = "number";
            domInput.step = "0.01";
            break;
        case "int":
            domInput.type = "number";
            break;
        case "boolean":
            domInput.type = "checkbox";
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
    domForm.addEventListener('submit', (event) => {
        submit(event, methodName, domForm);
    });
    // domForm.onsubmit="submit(event)";

    // TODO: Implement methods with no arguments and method overloads
    if (args.length !== 0) {
        args[0].forEach(input => {
            const domInput = dataTypeToInputType(input);
            domForm.appendChild(domInput);
        });
    }


    const domButton = document.createElement("input");
    domButton.type = "submit";
    domButton.value = methodName;
    domForm.appendChild(domButton);

    domInputs.appendChild(domForm);

});