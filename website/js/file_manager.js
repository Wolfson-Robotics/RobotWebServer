(async() => {


    async function getDirectoryListing(path) {
        const req = await fetch(`/file/listing?path=${encodeURIComponent(path)}`);
        if (!req.ok) {
            throw new Error(`Error loading directory ${path}: ${req.statusText}`);
        }
        return req.json();
    }

    async function fileOperation(action, data) {
        const res = window.callAPI("/file/file_operation", { action, ...data });
        if (!res.ok) throw new Error(`File operation error: ${res.statusText}`);
        return res.json();
    }

    async function directoryOperation(action, data) {
        const res = window.callAPI("/file/directory_operation", { action, ...data });
        if (!res.ok) throw new Error(`Directory operation error: ${res.statusText}`);
        return res.json();
    }



    const contextMenu = document.ofId("div", "rfe-context-menu");
    document.body.appendChild(contextMenu);

    // Hide context menu on click
    document.addEventListener("click", () => {
        contextMenu.hide();
        clearSelected();
    });
    window.addEventListener("resize", contextMenu.hide);



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

    function buildContextMenu(type, targetItem, evt) {

        const fileActions = ["Download", "Edit", "Copy", "Rename", "Delete"];
        const dirActions = ["Create new file", "Create new directory", "Copy", "Rename", "Delete"];
        const actions = type === "dir" ? dirActions : fileActions;

        contextMenu.innerHTML = "";

        actions.forEach(action => {
            const menuItem = document.createElement("div");
            menuItem.addClass("rfe-context-menu-item");
            menuItem.textContent = action;

            if (action === actions.lastElem()) {
                menuItem.style.borderBottom = "none";
            }
            // TODO: Stub click handlers
            menuItem.addEventListener("click", (e) => {
                e.stopPropagation();
                console.log(`${action} clicked on ${type} "${targetItem.dataset.fullPath}"`);
                contextMenu.hide();
            });
            contextMenu.appendChild(menuItem);
        });

        contextMenu.style.left = evt.pageX + "px";
        contextMenu.style.top = evt.pageY + "px";
        contextMenu.show();
    }






    async function loadDirIntoTree(items, path, subtree){
        items.dirs.forEach(dir => {
            subtree.appendChild(createListItem(dir, "dir", path ? `${path}/${dir}` : dir));
        });
        items.files.forEach(file => {
            subtree.appendChild(createListItem(file, "file", path ? `${path}/${file}` : file));
        })
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
        // li.textContent = name;
        li.dataset.fullPath = fullPath;

        const content = document.createElement("div");
        content.addClass("rfe-item-content");

        if (type === "dir") {

            const arrow = document.createElement("span");
            arrow.addClass("rfe-arrow");
            content.appendChild(arrow);

            // icon.addClass("rfe-arrow");
        } else {
            const placeholder = document.createElement("span");
            placeholder.addClass("rfe-arrow-placeholder");
            content.appendChild(placeholder);
        }
        // li.prepend(icon);
        // li.appendChild(content);
        content.appendChild(document.elemOf("span", {
            className: "rfe-icon",
            innerHTML: type === "dir" ? folderSVG : fileSVG
        }));

        content.appendChild(document.elemOf("span", {
            className: "rfe-name",
            textContent: name
        }))


        li.appendChild(content);

        if (type === "dir") {
            content.addEventListener("click", async(e) => {
                // e.stopPropagation();
                if (li.hasClass("expanded")) {
                    collapseNode(li);
                } else {
                    await createSubtree(fullPath, li);
                }
            });
        }


        li.addEventListener("click", (e) => {
            e.stopPropagation();
            clearSelected();
            contextMenu.hide();
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



    async function init(container, basePath) {
        container.innerHTML = "";
        const treeRoot = document.createElement("ul");
        treeRoot.classList.add("rfe-tree");
        container.appendChild(treeRoot);

        const items = await getDirectoryListing(basePath);
        await loadDirIntoTree(items, basePath, treeRoot);
    }

    init(document.getElementById("explorer"), "");
})();
