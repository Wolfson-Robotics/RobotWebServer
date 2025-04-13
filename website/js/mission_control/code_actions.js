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
            if (!stop) {
                highlightLine(index);
                await controlLib.callMethod(methodsToRun[index]).catch(err => {
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
    stopBtn.addEventListener("click", () => {
        stop = true;
        if (pause) pause.resolve();
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





    function downloadTextFile(text) {
        const now = new Date();
        const doubleDigit = (n) => String(n).padStart(2, '0');

        const blob = URL.createObjectURL(new Blob([text], { type: 'text/plain' }));

        const a = document.createElement('a');
        a.href = blob;
        a.download = `code-editor-${now.getFullYear()}${doubleDigit(now.getMonth() + 1)}${doubleDigit(now.getDate())}-${doubleDigit(now.getHours())}${doubleDigit(now.getMinutes())}${doubleDigit(now.getSeconds())}.txt`;
        a.click();

        URL.revokeObjectURL(blob);
    }

    function getTextFromFile() {
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

                const reader = new FileReader();
                reader.onload = (e) => resolve(e.target.result);
                reader.onerror = (e) => reject('Error reading file.');
                reader.readAsText(file);
            });

            fileInput.click();
        });
    }


    document.getElementById("save").addEventListener("click", () => downloadTextFile(window.editor.getValue()));
    document.getElementById("load").addEventListener("click", () => {
        if (!window.editor.isEmpty() && !confirm("Loading from a file will erase the code currently in the editor. Are you sure you want to coninue?")) {
            return;
        }
        getTextFromFile().then(text => window.editor.setValue(text)).catch(err => {
            console.error(err);
            alert("An error occurred loading the text from the file. Please see the console for more details.");
        });
    });

}