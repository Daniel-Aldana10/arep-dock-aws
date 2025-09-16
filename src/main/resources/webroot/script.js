function loadGetMsg() {
    const nameVar = document.getElementById("name").value;
    if (!nameVar) {
        document.getElementById("getrespmsg").innerHTML = "Please enter a name.";
        return;
    }
    const url = "/app/helloget?name=" + encodeURIComponent(nameVar);

    fetch(url, { method: 'GET' })
        .then(response => response.json())
        .then(text => {
            document.getElementById("getrespmsg").innerHTML = text.msg || text;
        })
        .catch(err => {
            document.getElementById("getrespmsg").innerHTML = "Error: " + err;
        });
}

function loadPostMsg(nameInput) {
    if (!nameInput.value) {
        document.getElementById("postrespmsg").innerHTML = "Please enter a name.";
        return;
    }
    const url = "/app/hellopost?name=" + encodeURIComponent(nameInput.value);

    fetch(url, { method: 'POST' })
        .then(response => response.json())
        .then(msg => {
            document.getElementById("postrespmsg").innerHTML = msg.msg || msg;
        })
        .catch(err => {
            document.getElementById("postrespmsg").innerHTML = "Error: " + err;
        });
}