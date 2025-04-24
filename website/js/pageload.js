(async() => {


function pageload() {
    const jsToLoad = ["mission_control", "robot_devices", "telemetry", "file_manager", "camera_feed"];
    jsToLoad.forEach(js => document.body.appendChild(document.elemOf("script", {
        src: "/js/" + js + ".js",
        type: "text/javascript"
    })));
}


fetch("http://localhost:8080/config.json")
    .then(req => {
        if (!req.ok) {
            throw new Error(`Config.json not found, responded with ${req.status}.`);
        }
        return req;
    })
    .then(res => res.json())
    .catch(err => {
        alert("Failed to load config.json. Check the console for more details. Unexpected issues may occur.");
        console.error(err);
        pageload();
    })
    .then(data => {
        window.config = data;

        const pageTitle = document.getElementById("page-title");
        pageTitle.innerHTML = "FTC-" + data.team_number + " Robot Web Dashboard";
        document.title = "FTC-" + data.team_number + " Robot Web Dashboard";

        pageload();
    });



window.fixPath = (path) => path.replace(/\/+/g, "/");
window.getAPI = (endpoint) => fetch(`${window.location.origin}/${fixPath(endpoint)}`);
window.callAPI = (endpoint, payload) => {
    return fetch(`${window.location.origin}/${fixPath(endpoint)}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: typeof payload === "string" ? payload : JSON.stringify(payload),
    });
};


window.startSocket = (endpoint) => new WebSocket(`ws://localhost:9090/${endpoint}`);
window.aliveSocket = (socket, message = "ping", opName = String(Math.random()), timeout = 5000) => {

    if (!socket.running) socket.running = {};
    socket.running[opName] = window.setInterval(() => {
        if (typeof message === "function") {
            message();
            return;
        }
        socket.send(message);
    }, timeout);
    return opName;

};
window.stopAliveSocket = (socket, opName) => {
    window.clearInterval(socket.running[opName]);
    delete socket.running[opName];
};
window.persistSocket = (socket, callable) => {
    socket.onerror = callable;
};



window.getHumanDate = () => {
    const now = new Date();
    const doubleDigit = (n) => String(n).padStart(2, '0');
    return `${now.getFullYear()}${doubleDigit(now.getMonth() + 1)}${doubleDigit(now.getDate())}-${doubleDigit(now.getHours())}${doubleDigit(now.getMinutes())}${doubleDigit(now.getSeconds())}`;
};

window.downloadTextFile = (text, path = "") => {

    const blob = URL.createObjectURL(new Blob([text], { type: 'text/plain' }));

    const a = document.createElement('a');
    a.href = blob;
    a.download = path === "" ? `${getHumanDate()}.txt` : path;
    a.click();

    URL.revokeObjectURL(blob);
};

window.getFileChoice = () => {
    return new Promise((resolve, reject) => {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = '.txt';

        fileInput.addEventListener('change', () => {
            const file = fileInput.files[0];
            if (!file) {
                reject('No file selected.');
                return;
            }
            resolve(file);
        });

        fileInput.click();
    });
};
window.getTextFromFile = (file) => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.onerror = (e) => reject('Error reading file.');
        reader.readAsText(file);
    });
}



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
Array.prototype.filterDefined = function() {
    return this.filter(Boolean);
};

// Define array operations for NodeLists to prevent boilerplate Array.from conversions
Object.getOwnPropertyNames(Array.prototype)
    .filter(prop => typeof Array.prototype[prop] === 'function')
    .filter(prop => !NodeList.prototype[prop])
    .forEach(oper => {
        NodeList.prototype[oper] = function(...args) {
            return Array.from(this)[oper].apply(this, args);
        };
    });



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
Object.prototype.jsonMap = function(keyMap, valMap) {
    const newJSON = {};
    Object.entries(this).forEach(([k, v], i) => {
        newJSON[keyMap(k, i)] = valMap(v, i);
    });
    return newJSON;
};
Object.prototype.keyMap = function(keyMap) {
    return this.jsonMap(keyMap, v => v);
};
Object.prototype.valMap = function(valMap) {
    return this.jsonMap(k => k, valMap);
};

Math.isInt = (number) => {
    if (typeof number !== "number") {
        return false;
    }
    return Math.floor(number) === number;
};
String.prototype.count = function(char) {
    return this.split(char).length - 1;
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
document.elemOf = (tag, info) => {
    const elem = document.createElement(tag);
    if (info) Object.entries(info).forEach(([prop, propVal]) => elem[prop] = propVal);
    return elem;
};
document.ofId = (tag, id) => {
    return document.elemOf(tag, { id: id });
};
document.ofClass = (tag, className) => document.elemOf(tag, { className: className });

window.reverseJSON = (json) => {
    const reversed = {};
    Object.entries(json).forEach(([key, val]) => {
        if (!reversed[val]) reversed[val] = [];
        reversed[val].push(key);
    });
    return reversed;
}




})();