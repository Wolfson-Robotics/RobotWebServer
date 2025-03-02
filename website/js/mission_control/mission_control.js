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

    const amREQ = await fetch(window.location.protocol + "/robot?get=all_methods");
    const amRES = await amREQ.json();
    if (amRES.error) {
        alert("An error occurred fetching the robot methods. Details:\n" + amRES.error);
        return;
    }

    window.robotMethods = amRES;
    loadScriptAsync("/js/mission_control/code_editor.js");

})();