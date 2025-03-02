function submit(event, methodName, form) {
    event.preventDefault();

    var json = {};
    json["name"] = methodName;

    var formData = new FormData(form);
    formData.forEach((value, key) => {
        args.push(value);
    });

    json["args"] = [];
    for (var input of form.elements) {
        switch (input.type) {
            case "submit":
                continue;
            case "double":
            case "number":
                json["args"].push(Number(input.value));
                continue;
            case "checkbox":
                json["args"].push(input.checked);
                continue;
            default:
                json["args"].push(input.value);
                continue;
        }
 
    }

    console.log(JSON.stringify(json));

    fetch(window.location.protocol + '/robot?post=call_method', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body:JSON.stringify(json),
    });
}

function dataTypetoInputType(dataType) {
    switch (dataType) {
        case "double":
        case "int":
            return "number";
        case "boolean":
            return "checkbox";
        case "String":
            return "text";
        default:
            return "text";
    }
}

function setupInputs(inputs) {
    console.log("Processing inputs...")
    var forms = Object.keys(inputs);
    const domInputs = document.getElementById("inputs");

    for ( const form of forms) {
        const domForm = document.createElement("form");
        domForm.id = form;
        domForm.addEventListener('submit', function (event) {
            submit(event, form, domForm);
        });
        domForm.onsubmit="submit(event)"

        var inputsToCreate = inputs[form];

        if (inputsToCreate.length != 0) {
            for (var input of inputsToCreate[0]) {
                var domInput = document.createElement("input");
                domInput.type = dataTypetoInputType(input);
                domInput.required = true;
                domForm.appendChild(domInput);
            }
        }
        

        var domButton = document.createElement("input");
        domButton.type = "submit";
        domButton.value = form;
        domForm.appendChild(domButton);

        domInputs.appendChild(domForm);
    }
}

fetch("http://localhost:8080/robot?get=all_methods")
    .then(response => response.json())
    .then(data => { setupInputs(data); });

fetch("http://localhost:8080/robot?get=team_number")
    .then(response => response.json())
    .then(data => {
        const pageTitle = document.getElementById("page-title");
        pageTitle.innerHTML = "FTC-" + data["team_number"] + " Robot Web Dashboard";
        document.title = "FTC-" + data["team_number"] + " Robot Web Dashboard";
    });
