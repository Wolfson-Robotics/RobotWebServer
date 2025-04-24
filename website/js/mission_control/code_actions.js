export function run(controlLib) {

    function clearHighlights() {
        window.editor.eachLine(line => {
            window.editor.removeLineClass(line, "wrap", "faded-line");
            window.editor.removeLineClass(line, "wrap", "highlight-line");
        });
    }
    function highlightLine(lineNumber) {
        const doc = window.editor.getDoc();

        clearHighlights();

        window.editor.eachLine(line => {
            window.editor.addLineClass(line, "wrap", "faded-line");
        });

        const handle = doc.getLineHandle(lineNumber);
        window.editor.removeLineClass(handle, "wrap", "faded-line");
        window.editor.addLineClass(handle, "wrap", "highlight-line");
    }



    let resolvePause;
    let pause;
    let stop = false;

    const [runBtn, stopBtn, pauseBtn, resumeBtn] =
        ["run", "stop", "pause", "resume"]
        .map(id => document.getElementById(id));


    runBtn.addEventListener("click", async() => {
        stop = false;
        pause = undefined;

        runBtn.hide();
        stopBtn.show();
        pauseBtn.show();
        // Use for loop instead of forEach to ensure successive execution
        const methodsToRun = window.editor.textToMethods(window.editor.getValue());
        for (let index = 0; index < methodsToRun.length; index++) {

            if (pause) {
                await pause.then(() => pause = undefined);
            }
            const info = methodsToRun[index];
            if (!stop) {
                highlightLine(info[0]);
                await controlLib.callMethod(info[1]).catch(err => {
                    stop = true;
                    console.error(err);
                    alert(`An error occurred calling method ${method.name} at line ${index + 1}. Please see the console for more details.`);
                });
            }

        }

        clearHighlights();
        stopBtn.hide();
        pauseBtn.hide();
        runBtn.show();
    });
    stopBtn.addEventListener("click", async() => {
        stop = true;
        if (pause) resolvePause();
    });


    pauseBtn.addEventListener("click", () => {
        pause = new Promise(resolve => resolvePause = resolve);
        pauseBtn.hide();
        resumeBtn.show();
    });
    resumeBtn.addEventListener("click", () => {
        resolvePause();
        resumeBtn.hide();
        pauseBtn.show();
    });





    document.getElementById("save").addEventListener("click", () => window.downloadTextFile(window.editor.getValue(), `code-editor-${getHumanDate()}.txt`));
    document.getElementById("load").addEventListener("click", () => {
        if (!window.editor.isEmpty() && !confirm("Loading from a file will erase the code currently in the editor. Are you sure you want to coninue?")) {
            return;
        }
        window.getFileChoice().then(file => window.getTextFromFile(file)).then(text => window.editor.setValue(text)).catch(err => {
            console.error(err);
            alert("An error occurred loading the text from the file. Please see the console for more details.");
        });
    });

}