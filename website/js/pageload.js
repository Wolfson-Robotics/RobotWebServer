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
Array.prototype.lastElem = function() {
    return this[this.length - 1];
};


window.toLines = (text) => {
    return text.replaceAll("\r", "").split("\n");
}
// General element helper methods
HTMLElement.prototype.hide = function() { this.style.display = "none"; };
HTMLElement.prototype.show = function() { this.style.display = "block"; };
HTMLElement.prototype.addClass = function(clazz) { this.classList.add(clazz); }
HTMLElement.prototype.removeClass = function(clazz) { this.classList.remove(clazz); };
HTMLElement.prototype.hasClass = function(clazz) { return this.classList.contains(clazz); };

// Custom constructor helpful for our purposes
// No arrow functions to preserve this for element operations
document.elemOf = function(tag, info) {
    const elem = document.createElement(tag);
    Object.entries(info).forEach(([prop, propVal]) => elem[prop] = propVal);
    return elem;
};
document.ofId = function(tag, id) {
    return document.elemOf(tag, { id: id });
};
document.ofClass = (tag, className) => document.elemOf(tag, { className: className });