(async() => {

    function errMsg(err, action, data) {
        alert(`An error occurred with executing action ${action} on ${data.path}: \n${err.message ? err.message : err}`);
    }
    function commonCatch(err, action, data) {
        errMsg(err, action, typeof data === "object" ? data : { path: data });
        //throw err;
    }

    async function commonAPIHandle(req, hereCatch) {
        return req.catch(hereCatch)
            .then(res => res.json())
            .catch(hereCatch)
            .then(json => {
                if (json.error) hereCatch(json.error);
                return json;
            })
            .catch(hereCatch);
    }

    async function getDirectoryListing(path) {
        return commonAPIHandle(
            fetch(`${window.getEndpoint("fileListing")}?path=${encodeURIComponent(path)}`),
            (err) => commonCatch(err, "listing", path)
        );
    }

    async function getFile(path) {
        return window.callAPI(`${window.config.fileManager.fileEndpointPath}/file_operation`, { action: "get", path: path })
            .catch(err => commonCatch(err, "get", path))
            .then(res => res.text())
            .catch(err => commonCatch(err, "get", path));
    }

    async function commonOperation(endpoint, action, data) {
        return commonAPIHandle(
            window.callAPI(`${window.config.fileManager.fileEndpointPath}/${endpoint}_operation`, { action: action, ...(typeof data === "string" ? { path: data } : data) }),
            (err) => commonCatch(err, action, data)
        );
    }

    async function fileOperation(action, data) {
        return commonOperation("file", action, data);
    }
    async function directoryOperation(action, data) {
        return commonOperation("directory", action, data);
    }



    const contextMenu = document.ofId("div", "rfe-context-menu");
    document.body.appendChild(contextMenu);

    // Hide context menu on click
    document.addEventListener("click", () => {
        contextMenu.hide();
        clearSelected();
        selected = false;
    });


    let selected = false;
    let currLi = null;
    document.addEventListener("keydown", (e) => {
        if (!selected || !currLi) return;

        // Map actions as JSON to prevent default if the case is not met, which
        // is something that cannot be done with switch loops
        const keyActions = {
            ArrowRight: () => {
                if (isFile(currLi.dataset.fullPath)) return;
                if (!currLi.hasClass("expanded")) createSubtree(currLi.dataset.fullPath, currLi);
            },
            ArrowLeft: () => {
                if (isFile(currLi.dataset.fullPath)) return;
                collapseNode(currLi);
            },
            ArrowDown: () => {
                const allLis = document.querySelectorAll("li.rfe-item");
                const index = allLis.indexOf(currLi);
                if (index >= allLis.length - 1) {
                    return;
                }
                const thisLi = allLis[index + 1];
                const bottom = thisLi.getBoundingClientRect().bottom;
                if (bottom > window.innerHeight) {
                    window.scrollBy({top: bottom - window.innerHeight});
                }
                allLis[index + 1].click();

            },
            ArrowUp: () => {
                const allLis = document.querySelectorAll("li.rfe-item");
                const index = allLis.indexOf(currLi);
                if (index <= 0) {
                    return;
                }
                const thisLi = allLis[index - 1];
                const top = thisLi.getBoundingClientRect().top;
                if (top < 0) {
                    window.scrollBy({top: top});
                }
                thisLi.click();
            }
        };
        const fn = keyActions[e.key || e.code];
        if (fn) {
            e.preventDefault();
            fn();
        }
    });


    window.addEventListener("resize", contextMenu.hide);

    const fileActions = ["Download", "Edit", "Copy", "Rename", "Delete"];
    const dirActions = ["Create new file", "Create new directory", "Upload file", "Copy", "Paste", "Rename", "Delete", "Refresh"];

    const folderSVG = `
        <svg viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg">
            <path fill="#666" d="M10 4H2v16h20V6H12z"/>
        </svg>
    `;

    const fileSVG = `
        <svg class="file-icon" viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg">
            <path fill="#666" d="M14 2H6v20h12V8l-4-6z"/>
        </svg>
    `;


    let fileClipboard = "";

    function buildContextMenu(type, targetItem, evt) {

        const actions = type === "dir" ? dirActions : fileActions;

        contextMenu.innerHTML = "";

        actions.forEach(action => {
            const menuItem = document.createElement("div");
            menuItem.addClass("rfe-context-menu-item");
            menuItem.textContent = action;
            menuItem.dataset.action = action.toLowerCase().trim().replaceAll(" ", "-");

            if (action === actions.lastElem()) {
                menuItem.style.borderBottom = "none";
            }

            menuItem.addEventListener("click", (e) => {
                e.stopPropagation();
                console.log(`${action} clicked on ${type} "${targetItem.dataset.fullPath}"`);
                menuListener(action, e.target, targetItem);
                contextMenu.hide();
            });
            contextMenu.appendChild(menuItem);
        });

        contextMenu.style.left = evt.pageX + "px";
        contextMenu.style.top = evt.pageY + "px";
        // Show element to obtain bounding rect and hide to adjust before finally showing
        contextMenu.show();
        const rect = contextMenu.getBoundingClientRect();
        contextMenu.hide();
        if ((rect.width + evt.pageX) > (window.innerWidth + window.scrollX)) {
            contextMenu.style.left = (evt.pageX - rect.width) + "px";
        }
        if ((rect.height + evt.pageY) > (window.innerHeight + window.scrollY)) {
            contextMenu.style.top = (evt.pageY - rect.height) + "px";
        }
        contextMenu.show();
    }




    function validatePath(input) {
        if (typeof input !== "string" || input.trim() === "") {
            throw new Error("Path must be a non-empty string");
        }
        if (input.includes("\0")) {
            throw new Error("Path cannot contain null characters");
        }
        if (input.includes("\\")) {
            throw new Error("Backslashes are not permitted");
        }

        const components = input.split("/").filter(comp => comp !== "").map(comp => comp.trim());
        // Don't do forEach for direct returning of the function
        for (const comp of components) {
            if (comp.length > 255) {
                throw new Error("A path segment cannot exceed 255 characters");
            }
        }

        return true;
    }

    function validateFilePath(input) {
        try {
            validatePath(input);
        } catch (e) {
            return e;
        }

        if (input.endsWith("/") || input.endsWith("..")) {
            throw new Error("The path provided is a directory");
        }
        if (input.endsWith(".") || !input.includes(".")) {
            throw new Error("No file extension provided");
        }
        if (input.count(".") !== 1) {
            throw new Error("More than one file extension provided");
        }
        return true;
    }

    function validateDirectoryPath(input) {
        try {
            validatePath(input);
        } catch (e) {
            return e;
        }

        try {
            return !validateFilePath(input);
        } catch (e) {
            return true;
        }
    }

    function isFile(input) {
        try { return validateFilePath(input); } catch (e) { return false; }
    }
    function isDirectory(input) {
        try { return validateDirectoryPath(input); } catch (e) { return false; }
    }

    function getParentPath(path) {
        const idx = path.lastIndexOf("/");
        return idx === -1 ? "" : path.substring(0, idx);
    }

    function getPrincipalName(path) {
        const comps = path.split("/");
        return comps.length < 2 ? path : comps.lastElem();
    }

    function getExtension(path) {
        const comps = path.split(".");
        return comps.length === 2 && comps.lastElem();
    }

    function fireUpdate(fullPath, newPath) {
        const payload = newPath ? { path: fullPath, to: newPath } : { path: fullPath };
        window.dispatchEvent(new CustomEvent("rfe:updateDirectory", { detail: payload }));
    }

    function menuListener(action, srcElem, targetElem) {

        const fullPath = targetElem.dataset.fullPath;
        const type = targetElem.dataset.type;
        const isDir = type === "dir";
        const principalName = getPrincipalName(fullPath);
        const hereOperation = isDir ? directoryOperation : fileOperation;

        switch (srcElem.dataset.action) {

            case "download":
                getFile(fullPath).then(text => window.downloadTextFile(text, principalName));
                break;
            case "edit":
                if (!window.config.fileManager.editableFiles.includes(getExtension(fullPath))) {
                    infoDialog("Cannot Edit", "Only plaintext or log files may be edited");
                    return;
                }
                getFile(fullPath).then(text =>
                    textInputDialog(`Editing ${principalName}`, text).then(input =>
                        fileOperation("write", { path: fullPath, content: input })
                    )
                );
                break;
            case "copy":
            case "copy-directory":
                fileClipboard = fullPath;
                break;
            case "rename":
                fileInputDialog("Rename", isDir ? validateDirectoryPath : validateFilePath, fullPath)
                    .then(async(path) => {
                        await hereOperation.call(null, "rename", {
                            path: fullPath,
                            to: path
                        });
                        return path;
                    })
                    .then(to => {
                        const path = isDirectory(fullPath) ? getParentPath(fullPath) : fullPath;
                        if (isDirectory(to)) {
                            fireUpdate(path, to);
                        } else {
                            fireUpdate(path);
                        }
                    });
                break;
            case "delete":
                confirmDialog(`Delete ${isDir ? "directory" : "file"}`, `Are you sure you want to delete this ${isDir ? "directory" : "file"}? THIS CANNOT BE UNDONE.`)
                    .then(() => {
                        hereOperation.call(null, "delete", { path: fullPath }).then(() =>
                            fireUpdate(getParentPath(fullPath))
                        );
                    });
                break;
            case "create-new-file":
                fileInputDialog("Create new file", validateFilePath)
                    .then(path => fileOperation("create", { path: `${fullPath}/${path}` }))
                    .then(() =>
                        fireUpdate(fullPath)
                    );
                break;
            case "create-new-directory":
                fileInputDialog("Create new directory", validateDirectoryPath, fullPath)
                    .then(path => directoryOperation("create", { path: path + "/" }))
                    .then(() =>
                        fireUpdate(fullPath)
                    );
                break;
            case "upload-file":
                window.getFileChoice().then(file =>
                    window.getTextFromFile(file).then(text =>
                        fileOperation("create", { path: `${fullPath}/${file.name}`, content: text }).then(() =>
                            fireUpdate(fullPath)
                        )
                    )
                );
                break;
            case "paste":
                if (fileClipboard === "") {
                    infoDialog("Error", "There is no file currently in the clipboard.");
                    return;
                }
                (isFile(fileClipboard) ? fileOperation : directoryOperation).call(null, "copy", {
                    path: fileClipboard,
                    to: isDirectory(fullPath) ? `${fullPath}/${getPrincipalName(fileClipboard)}` : fullPath
                }).then(() => {
                    fireUpdate(fullPath);
                    fileClipboard = "";
                });
                break;
            case "refresh":
                fireUpdate(fullPath);
                break;

        }

    }



    function fileInputDialog(title, validator, init = "") {
        const resultFn = validator ? (val) => {
            try { return validator(val); } catch (e) { return e; }
        } : validator;

        return new Promise((resolve, reject) => {
            const overlay = document.ofClass("div", "rfe-dialog-overlay");
            const dialog = document.ofClass("div", "rfe-dialog");

            dialog.appendChild(document.elemOf("div", { className: "rfe-dialog-prompt", textContent: title }));

            const input = document.createElement("input");
            input.type = "text";
            input.value = init;
            input.addClass("rfe-dialog-input");
            dialog.appendChild(input);

            const btnContainer = document.ofClass("div", "rfe-dialog-buttons");

            const okBtn = document.createElement("button");
            okBtn.textContent = "OK";
            okBtn.addClass("rfe-dialog-btn");
            btnContainer.appendChild(okBtn);

            const cancelBtn = document.createElement("button");
            cancelBtn.textContent = "Cancel";
            cancelBtn.addClass("rfe-dialog-btn");
            btnContainer.appendChild(cancelBtn);

            dialog.appendChild(btnContainer);
            overlay.appendChild(dialog);
            document.body.appendChild(overlay);

            input.focus();
            input.addEventListener("change", () => {

                const result = resultFn ? resultFn(input.value) : true;
                if (typeof result === "boolean" && result) {
                    input.removeClass("rfe-input-invalid");
                    okBtn.disabled = false;
                } else {
                    input.addClass("rfe-input-invalid");
                    input.setCustomValidity(result || "Invalid file path.");
                    input.reportValidity();
                    okBtn.disabled = true;
                }

            });

            okBtn.addEventListener("click", () => {
                const value = input.value;
                document.body.removeChild(overlay);
                resolve(value);
            });

            cancelBtn.addEventListener("click", () => {
                document.body.removeChild(overlay);
                reject(false);
            });

        });
    }


    function infoDialog(title, message) {
        return new Promise((resolve) => {
            const overlay = document.ofClass("div", "rfe-dialog-overlay");
            const dialog = document.ofClass("div", "rfe-dialog");
            dialog.appendChild(document.elemOf("div", { className: "rfe-dialog-prompt", textContent: title }));
            dialog.appendChild(document.elemOf("div", { className: "rfe-dialog-message", textContent: message}));

            const btnContainer = document.ofClass("div", "rfe-dialog-buttons");

            const okBtn = document.elemOf("button", { className: "rfe-dialog-btn", textContent: "OK"});
            btnContainer.appendChild(okBtn);

            dialog.appendChild(btnContainer);
            overlay.appendChild(dialog);
            document.body.appendChild(overlay);

            okBtn.addEventListener("click", () => {
                document.body.removeChild(overlay);
                resolve();
            });
        });
    }


    function confirmDialog(title, message) {
        return new Promise((resolve, reject) => {
            const overlay = document.ofClass("div", "rfe-dialog-overlay");
            const dialog = document.ofClass("div", "rfe-dialog");

            dialog.appendChild(document.elemOf("div", { className: "rfe-dialog-prompt", textContent: title }));
            dialog.appendChild(document.elemOf("div", { className: "rfe-dialog-message", textContent: message }));

            const btnContainer = document.createElement("div");
            btnContainer.addClass("rfe-dialog-buttons");

            const yesBtn = document.elemOf("button", { className: "rfe-dialog-btn", textContent: "Yes" });
            const noBtn = document.elemOf("button", { className: "rfe-dialog-btn", textContent: "No" });

            btnContainer.appendChild(yesBtn);
            btnContainer.appendChild(noBtn);
            dialog.appendChild(btnContainer);
            overlay.appendChild(dialog);
            document.body.appendChild(overlay);

            yesBtn.addEventListener("click", () => {
                document.body.removeChild(overlay);
                resolve(true);
            });

            noBtn.addEventListener("click", () => {
                document.body.removeChild(overlay);
                reject(false);
            });
        });
    }


    function textInputDialog(title, init = "") {
        return new Promise((resolve, reject) => {
            const overlay = document.ofClass("div", "rfe-dialog-overlay");
            const dialog = document.ofClass("div", "rfe-dialog");

            dialog.appendChild(document.elemOf("div", { className: "rf-dialog-prompt", textContent: title }));
            const textarea = document.elemOf("textarea", { className: "rfe-dialog-textarea", "value": init});
            dialog.appendChild(textarea);

            const btnContainer = document.ofClass("div", "rfe-dialog-buttons");
            const submitBtn = document.elemOf("button", { className: "rfe-dialog-btn", textContent: "Save"});
            const cancelBtn = document.elemOf("button", { className: "rfe-dialog-btn", textContent: "Cancel"});
            btnContainer.appendChild(submitBtn);
            btnContainer.appendChild(cancelBtn);

            dialog.appendChild(btnContainer);
            overlay.appendChild(dialog);
            document.body.appendChild(overlay);

            submitBtn.addEventListener("click", () => {
                document.body.removeChild(overlay);
                resolve(textarea.value);
            });
            cancelBtn.addEventListener("click", async() => {

                if (textarea.value !== init) {
                    if (!(await confirmDialog("Discard changes?", "You have unsaved changes. Are you sure you want to discard them?"))) {
                        return;
                    }
                }
                document.body.removeChild(overlay);
                reject(false);

            });
        })
    }


    // Automatically sorts the paths by immediacy additionally
    function allOpen(li) {
        const allLis = li.querySelectorAll("li.rfe-item.expanded").map(item => item.dataset.fullPath);
        const depths = Object.fromEntries(allLis.map(((l, i) => [i, l.count("/")])));
        return Object.values(window.reverseJSON(depths).valMap(liIndices => liIndices.map(i => allLis[i]))).flat(1);
    }

    function getByPath(path) {
        return document.querySelector(`li.rfe-item[data-full-path="${path}"]`);
    }



    async function updateDirectory(json) {
        const path = isDirectory(json.path) ? json.path : getParentPath(json.path);
        const to = json.to;

        if (path === "") {
            const treeRoot = document.querySelector(".rfe-tree");
            const items = await getDirectoryListing(path);
            treeRoot.innerHTML = "";
            await loadDirIntoTree(items, path, treeRoot);
        } else {
            const li = getByPath(path);
            const allExpanded = allOpen(li);
            if (li && li.hasClass("expanded")) {

                const oldSubtree = li.querySelector("ul.rfe-subtree");
                await createSubtree(path, li);
                if (oldSubtree) li.removeChild(oldSubtree);

                allExpanded.forEach(p => {
                    const elem = getByPath(p);
                    if (elem) createSubtree(p, elem);
                });
                if (to) await createSubtree(to, getByPath(to));
            }
        }
    }

    window.addEventListener("rfe:updateDirectory", async(e) => updateDirectory(e.detail));





    async function loadDirIntoTree(items, path, subtree){
        items?.dirs?.forEach(dir => {
            subtree.appendChild(createListItem(dir, "dir", path ? `${path}/${dir}` : dir));
        });
        items?.files?.forEach(file => {
            subtree.appendChild(createListItem(file, "file", path ? `${path}/${file}` : file));
        });
    }

    async function createSubtree(fullPath, parentLI) {

        const items = await getDirectoryListing(fullPath);
        const subtree = document.createElement("ul");
        subtree.addClass("rfe-subtree");

        await loadDirIntoTree(items, fullPath, subtree);

        parentLI.appendChild(subtree);
        parentLI.addClass("expanded");

    }


    function clearSelected() {
        document.querySelectorAll(".rfe-item.selected").forEach(el => el.removeClass("selected"));
    }


    function createListItem(name, type, fullPath) {

        const li = document.createElement("li");
        li.addClass("rfe-item");
        li.dataset.type = type;
        li.dataset.fullPath = fullPath;

        const content = document.createElement("div");
        content.addClass("rfe-item-content");

        if (type === "dir") {
            const arrow = document.createElement("span");
            arrow.addClass("rfe-arrow");
            content.appendChild(arrow);
        } else {
            const placeholder = document.createElement("span");
            placeholder.addClass("rfe-arrow-placeholder");
            content.appendChild(placeholder);
        }
        content.appendChild(document.elemOf("span", {
            className: "rfe-icon",
            innerHTML: type === "dir" ? folderSVG : fileSVG
        }));

        content.appendChild(document.elemOf("span", {
            className: "rfe-name",
            // Hack fix for spacing between name and file icon
            ...(type === "file" ? { style: "padding-left: 2px;"} : {}),
            textContent: name
        }))


        li.appendChild(content);

        if (type === "dir") {
            content.addEventListener("click", (e) => {
                // e.stopPropagation();
                selected = true;
                currLi = li;
                if (li.hasClass("expanded")) {
                    collapseNode(li);
                } else {
                    createSubtree(fullPath, li);
                }
            });
        }


        li.addEventListener("click", (e) => {
            e.stopPropagation();
            clearSelected();
            contextMenu.hide();
            selected = true;
            currLi = li;
            li.addClass("selected");
        });

        li.addEventListener("contextmenu", (e) => {
            e.preventDefault();
            e.stopPropagation();
            clearSelected();
            li.addClass("selected");
            buildContextMenu(type, li, e);
        });
        return li;
    }

    function collapseNode(li) {
        li.removeClass("expanded");
        const subtree = li.querySelector("ul.rfe-subtree");
        if (subtree) li.removeChild(subtree);
    }



    async function init(container) {
        container.innerHTML = "";
        const treeRoot = document.ofClass("ul", "rfe-tree");
        await loadDirIntoTree({ dirs: ["/"] }, null, treeRoot);

        container.appendChild(treeRoot);
    }

    init(document.getElementById("explorer"));

})();
