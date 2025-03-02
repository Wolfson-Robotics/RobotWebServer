fetch("http://localhost:8080/robot/config.json")
    .then(response => response.json())
    .then(data => {
        const pageTitle = document.getElementById("page-title");
        pageTitle.innerHTML = "FTC-" + data.team_number + " Robot Web Dashboard";
        document.title = "FTC-" + data.team_number + " Robot Web Dashboard";
    });