// Drawer click listeners
var logo = document.querySelector("body #main #header .logo"),
    drawer = document.querySelector("body #main #header nav.primary"),
    buttonOpenDrawer = document.createElement("span"),
    buttonCloseDrawer = document.createElement("span");


buttonOpenDrawer.id = "buttonOpenDrawer";
buttonOpenDrawer.addEventListener("click", function() {
    drawer.classList.add("open");
});
logo.appendChild(buttonOpenDrawer);

buttonCloseDrawer.id = "buttonCloseDrawer";
buttonCloseDrawer.addEventListener("click", function() {
    drawer.classList.remove("open");
});
drawer.appendChild(buttonCloseDrawer);

// Song list hide/show click listeners
var songEntries = document.querySelectorAll("#post-7518 article #page-content .box .bg .inner div");
songEntries.forEach(function (songEntry, p2, p3) {
    songEntry.addEventListener("click", function () {
        this.classList.toggle("open");
    });
});
