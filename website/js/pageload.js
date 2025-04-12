fetch("http://localhost:8080/robot/config.json")
    .then(response => response.json())
    .then(data => {
        const pageTitle = document.getElementById("page-title");
        pageTitle.innerHTML = "FTC-" + data.team_number + " Robot Web Dashboard";
        document.title = "FTC-" + data.team_number + " Robot Web Dashboard";
    });


window.callAPI = (endpoint, payload) => {
    return window.fetch(`${window.location.protocol}/robot/${endpoint}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: typeof payload === "string" ? payload : JSON.stringify(payload),
    });
};

// Helper functions
Object.of = (...vals) => {
    if (vals.length % 2 !== 0) {
        throw new Error("The values provided are incomplete.");
    }
    const json = {};
    vals.filter((v, i) => i % 2 === 0).forEach((key, i) => {
        json[key] = vals[i + 1];
    });
    return json;
};
Object.prototype.map = function(keyMap, valMap) {
    const newJSON = {};
    Object.entries(this).forEach(([k, v], i) => {
        newJSON[keyMap(k, i)] = valMap(v, i);
    });
    return newJSON;
};
Object.prototype.keyMap = function(keyMap) {
    return this.map(keyMap, v => v);
};
Object.prototype.valMap = function(valMap) {
    return this.map(k => k, valMap);
};

Math.isInt = (number) => {
    if (typeof number !== "number") {
        return false;
    }
    return Math.floor(number) === number;
};
Array.prototype.remove = function(elem) {
    if (!Math.isInt(elem)) {
        throw new Error("Invalid index " + elem + ".");
    }
    this.splice(elem, 1);
};
Array.prototype.removeElem = function(elem) {
    const index = this.indexOf(elem);
    if (index < 0) {
        return;
    }
    this.remove(index);
};