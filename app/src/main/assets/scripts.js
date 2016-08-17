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