(async function robot_devices() {

    const root = document.getElementById("robot-devices");
    const deviceInfoSocket = window.startSocket(window.getEndpoint("deviceInfo"));
    deviceInfoSocket.onmessage = (e) => {


        JSON.parse(e.data).forEach(device => {

            const exists = document.querySelector(`li#${device.name}.robot-device`);
            const deviceElem = exists ??
                document.elemOf("li", { className: "robot-device", id: device.name });
            const deviceInfoList = exists ? exists.querySelector("div.device-info")
                : document.ofClass("div", "device-info");

            const type = device.type.toLowerCase().trim();
            const config = window.config.robot_devices;
            if (!exists) {

                const icons = config.icons;
                deviceInfoList.appendChild(document.elemOf("object", {
                    className: device.type.toLowerCase(),
                    data: `/img/${icons[type] ?? icons.unknown}`,
                    type: "image/svg+xml",
                    style: config.icons_css[type] ?? ""
                }));

                deviceInfoList.appendChild(document.elemOf("small", {textContent: device.type }));
                deviceInfoList.appendChild(document.elemOf("span", {innerHTML: `<b>${device.name}</b>`}));
                deviceInfoList.appendChild(document.elemOf("hr"));
            }


            Object.entries(device).forEach(([field, val]) => {
                if (["type", "name"].includes(field)) return;

                const fieldId = field.toLowerCase().replaceAll(" ", "-");
                const innerHTML = `<b>${field}</b>: ${val}`;
                if (exists) {
                    exists.querySelector(`span#${fieldId}`).innerHTML = innerHTML;
                } else {
                    deviceInfoList.appendChild(document.elemOf("span", {
                        id: fieldId,
                        innerHTML: innerHTML
                    }));
                }
            });

            if (!exists) {
                deviceElem.appendChild(deviceInfoList);
                root.appendChild(deviceElem);
            }

        });

    };

    window.aliveSocket(deviceInfoSocket);
    window.persistSocket(deviceInfoSocket);

})();