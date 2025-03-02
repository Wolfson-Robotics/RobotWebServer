fetch("http://localhost:8080/robot?get=all_methods")
    .then(response => response.json())
    .then(data => console.log(data));

fetch("http://localhost:8080/robot?get=team_number")
    .then(response => response.json())
    .then(data => {
        const pageTitle = document.getElementById("page-title");
        pageTitle.innerHTML = "FTC-" + data["team_number"] + " Robot Web Dashboard";
        document.title = "FTC-" + data["team_number"] + " Robot Web Dashboard";
    });